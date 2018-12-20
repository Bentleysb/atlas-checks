package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.index.PackedSpatialIndex;
import org.openstreetmap.atlas.geography.index.RTree;
import org.openstreetmap.atlas.geography.index.SpatialIndex;
import org.openstreetmap.atlas.tags.BuildingPartTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author bbreithaupt
 */
public class OrphanedBuildingPartsCheck extends BaseCheck
{

    private static final long serialVersionUID = 4156297664229919823L;

    private static final RelationOrAreaToMultiPolygonConverter MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Relation {0,number,#} is a building part and is not in a building relation or overlapping a building footprint.",
            "Way {0,number,#} is a building part and is not in a building relation or overlapping a building footprint.");

    private final Map<Atlas, SpatialIndex<Relation>> relationSpatialIndices = new HashMap<>();

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public OrphanedBuildingPartsCheck(final Configuration configuration)
    {
        super(configuration);
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
        return (object instanceof Area || object instanceof Relation)
                && Validators.isNotOfType(object, BuildingPartTag.class, BuildingPartTag.NO);
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
        if (((AtlasEntity) object).relations().stream().noneMatch(relation -> Validators
                .isOfType(relation, RelationTypeTag.class, RelationTypeTag.BUILDING))
                && !overlapsBuilding(object))
        {
            return object instanceof Relation
                    ? Optional.of(this.createFlag(((Relation) object).flatten(),
                            this.getLocalizedInstruction(0, object.getOsmIdentifier())))
                    : Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(1, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    private boolean overlapsBuilding(final AtlasObject part)
    {
        final Rectangle partBounds = part.bounds();
        if (!this.relationSpatialIndices.containsKey(part.getAtlas()))
        {
            this.relationSpatialIndices.put(part.getAtlas(),
                    this.buildRelationSpatialIndex(part.getAtlas()));
        }
        return part.getAtlas()
                .areasIntersecting(partBounds,
                        area -> BuildingTag.isBuilding(area) && neighboringPart(area, part))
                .iterator().hasNext()
                || this.relationSpatialIndices.get(part.getAtlas())
                        .get(partBounds, area -> neighboringPart(area, part)).iterator().hasNext();
    }

    /**
     * Checks if two {@link AtlasObject}s are building parts and overlap each other.
     *
     * @param part
     *            a known building part to check against
     * @return true if {@code object} is a building part and overlaps {@code part}
     */
    private boolean neighboringPart(final AtlasObject object, final AtlasObject part)
    {
        try
        {
            // Get the polygons of the parts, either single or multi
            final GeometricSurface partPolygon = part instanceof Area ? ((Area) part).asPolygon()
                    : MULTI_POLYGON_CONVERTER.convert((Relation) part);
            final GeometricSurface objectPolygon = object instanceof Area
                    ? ((Area) object).asPolygon()
                    : MULTI_POLYGON_CONVERTER.convert((Relation) object);
            // Check if it is a building part, and overlaps.
            return (partPolygon instanceof Polygon ? objectPolygon.overlaps((Polygon) partPolygon)
                    : objectPolygon.overlaps((MultiPolygon) partPolygon));
        }
        // Ignore malformed MultiPolygons
        catch (final CoreException invalidMultiPolygon)
        {
            return false;
        }
    }

    /**
     * Create a new spatial index that pre filters building relations. Pre-filtering drastically
     * decreases runtime by eliminating very large non-building relations. Copied from
     * {@link org.openstreetmap.atlas.geography.atlas.AbstractAtlas}.
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Relation> buildRelationSpatialIndex(final Atlas atlas)
    {
        final SpatialIndex<Relation> index = new PackedSpatialIndex<Relation, Long>(new RTree<>())
        {
            @Override
            protected Long compress(final Relation item)
            {
                return item.getIdentifier();
            }

            @Override
            protected boolean isValid(final Relation item, final Rectangle bounds)
            {
                return item.intersects(bounds);
            }

            @Override
            protected Relation restore(final Long packed)
            {
                return atlas.relation(packed);
            }
        };
        atlas.relations(relation -> relation.isMultiPolygon() && BuildingTag.isBuilding(relation))
                .forEach(index::add);
        return index;
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
