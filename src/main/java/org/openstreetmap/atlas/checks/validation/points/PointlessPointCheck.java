package org.openstreetmap.atlas.checks.validation.points;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.pbf.store.TagMap;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

/**
 * This check flags {@link Point}s that only contain pointless tags. Pointless tags are defined by
 * the {@code pointlessTagsFilter}.
 *
 * @author bbreithaupt
 */
public class PointlessPointCheck extends BaseCheck
{

    private static final long serialVersionUID = 3648467506199366032L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Node {0,number,#} is pointless.");

    private static final String POINTLESS_TAGS_FILTER_DEFAULT = "created_by->*|fixme->*|source->*|source:date->*|height->*|attribution->*";

    private final TaggableFilter pointlessTagsFilter;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public PointlessPointCheck(final Configuration configuration)
    {
        super(configuration);
        this.pointlessTagsFilter = (TaggableFilter) configurationValue(configuration,
                "pointless_tags.filter", POINTLESS_TAGS_FILTER_DEFAULT,
                value -> new TaggableFilter(value.toString()));
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Point && pointlessTagsFilter.test(object)
                && ((Point) object).relations().size() == 0;
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Get the objects tags
        final Map<String, String> tags = object.getOsmTags();
        // For each tag, make it taggable and test it against the pointlessTagsFilter
        for (final String tagKey : tags.keySet())
        {
            final Tag tagPair = new Tag(tagKey, tags.get(tagKey));
            // If the tag is not in the filter it is not pointless and this object should not be
            // flagged
            if (!pointlessTagsFilter.test(new TagMap(Collections.singletonList(tagPair))))
            {
                return Optional.empty();
            }
        }
        // Test to see if it is likely part of a way, and don't flag if so. Likely not if there are
        // overlapping nodes, and likely so if it matches a locations in a LineItem or Area.
        final Location objectLocation = ((Point) object).getLocation();
        int pointCount = 0;
        final Iterator points = object.getAtlas().pointsAt(objectLocation).iterator();
        while (points.hasNext())
        {
            pointCount++;
            points.next();
        }
        if (pointCount == 1
                && (object.getAtlas().lineItemsContaining(objectLocation).iterator().hasNext())
                || object.getAtlas()
                        .areasCovering(objectLocation,
                                area -> area.asPolygon().contains(objectLocation))
                        .iterator().hasNext())
        {
            return Optional.empty();
        }

        return Optional.of(this.createFlag(object,
                this.getLocalizedInstruction(0, object.getOsmIdentifier())));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
