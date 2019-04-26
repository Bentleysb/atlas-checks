package org.openstreetmap.atlas.checks.validation.tag;

import org.apache.commons.lang.StringUtils;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.names.AlternativeNameTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openstreetmap.atlas.checks.constants.CommonConstants.EMPTY_STRING;
import static org.openstreetmap.atlas.checks.constants.CommonConstants.SINGLE_SPACE;

/**
 * @author bbreithaupt
 */
public class ColumbiaAbbreviateStreetNamesCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -1185940533274517794L;

    private static final int NAME_GROUP_INDEX = 1;
    private static final int NAME_SUFFIX_GROUP_INDEX = 3;
    private static final List<String> STREET_TYPES_DEFAULT = Arrays.asList("Calle", "Carrera",
            "Transversal", "Diagonal");
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Way {0,number,#} can be abbreviated, but already has an alt name.");

    private final Pattern streetNamePattern;

    /**
     * @param configuration
     *            the JSON configuration for this check
     */
    public ColumbiaAbbreviateStreetNamesCheck(final Configuration configuration)
    {
        super(configuration);
        this.streetNamePattern = Pattern
                .compile(String.format("(.*(%s) \\d+)([\\p{Alpha} ]*)", String.join("|", this
                        .configurationValue(configuration, "street.types", STREET_TYPES_DEFAULT))));
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
        return object instanceof Edge && HighwayTag.isCarNavigableHighway(object)
                && object.getTag(NameTag.KEY).isPresent()
                && object.getTag(AlternativeNameTag.KEY).isPresent()
                && !this.isFlagged(object.getOsmIdentifier());
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
        final Matcher streetNameMatcher = streetNamePattern.matcher(object.tag(NameTag.KEY));
        if (streetNameMatcher.matches())
        {
            final String abbreviatedName = streetNameMatcher.group(NAME_GROUP_INDEX)
                    + StringUtils.capitalize(streetNameMatcher.group(NAME_SUFFIX_GROUP_INDEX)
                    .toLowerCase().replace(SINGLE_SPACE, EMPTY_STRING).replace("sur", "s"));

            if (!object.tag(NameTag.KEY).equals(abbreviatedName))
            {
                this.markAsFlagged(object.getOsmIdentifier());
                return Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(0, object.getOsmIdentifier())));
            }
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

}
