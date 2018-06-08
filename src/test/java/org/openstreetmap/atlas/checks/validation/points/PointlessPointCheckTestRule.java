package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * Tests for {@link PointlessPointCheck}
 *
 * @author bbreithaupt
 */

public class PointlessPointCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.2136626201459,-122.443275382856";

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "created_by=Bentleysb",
                    "fixme=pointless" }) })
    private Atlas pointlessPoint;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "created_by=Bentleysb",
                    "fixme=pointless", "amenity=waste_basket" }) })
    private Atlas usefulPoint;

    public Atlas pointlessPoint()
    {
        return this.pointlessPoint;
    }

    public Atlas usefulPoint()
    {
        return this.usefulPoint;
    }
}
