package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.BuildingPartTag;
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

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Relation {0,number,#} is a building part and is not in a building relation.",
            "Way {0,number,#} is a building part and is not in a building relation.");

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
                && Validators.isNotOfType(object, BuildingPartTag.class, BuildingPartTag.NO)
                && ((AtlasEntity) object).relations().stream().noneMatch(relation -> Validators
                        .isOfType(relation, RelationTypeTag.class, RelationTypeTag.BUILDING));
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
        return object instanceof Relation
                ? Optional.of(this.createFlag(((Relation) object).flatten(),
                        this.getLocalizedInstruction(0, object.getOsmIdentifier())))
                : Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(1, object.getOsmIdentifier())));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
