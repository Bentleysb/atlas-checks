package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * Tests for {@link PointlessPointCheck}
 *
 * @author bbreithaupt
 */

public class PointlessPointCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.2136626201459,-122.443275382856";
    private static final String TEST_2 = "47.2138327316739,-122.44258668766";
    private static final String TEST_3 = "47.2138327316739,-122.44248668766";
    private static final String TEST_4 = "47.2138327316739,-122.44238668766";

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

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_2), tags = { "created_by=Bentleysb",
                    "fixme=pointless" }) },
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_4) }, tags = { "highway=motorway" }) })
    private Atlas pointlessPointIntersectCorner;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_3), tags = { "created_by=Bentleysb",
                    "fixme=pointless" }) },
            // areas
            areas = { @Area(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_4),
                    @Loc(value = TEST_1) }, tags = { "highway=motorway" }) })
    private Atlas pointlessPointIntersectArea;

    @TestAtlas(
            // points
            points = {
                    @Point(coordinates = @Loc(value = TEST_2), tags = { "created_by=Bentleysb",
                            "fixme=pointless" }),
                    @Point(coordinates = @Loc(value = TEST_2), tags = {
                            "created_by=Bentleysb" }), },
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_4) }, tags = { "highway=motorway" }) })
    private Atlas pointlessPointIntersectCornerDuplicate;

    @TestAtlas(
            // points
            points = {
                    @Point(coordinates = @Loc(value = TEST_2), tags = { "created_by=Bentleysb",
                            "fixme=pointless" }),
                    @Point(coordinates = @Loc(value = TEST_2), tags = { "created_by=Bentleysb",
                            "amenity=waste_basket" }), },
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_4) }, tags = { "highway=motorway" }) })
    private Atlas pointlessPointIntersectCornerDuplicateUseful;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_3), tags = { "created_by=Bentleysb",
                    "fixme=pointless" }) },
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_4) }, tags = { "highway=motorway" }) })
    private Atlas pointlessPointIntersect;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_2), tags = { "created_by=Bentleysb",
                    "fixme=pointless" }) },
            // areas
            areas = { @Area(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_4),
                    @Loc(value = TEST_1) }, tags = { "highway=motorway" }) })
    private Atlas pointlessPointIntersectAreaCorner;

    public Atlas pointlessPoint()
    {
        return this.pointlessPoint;
    }

    public Atlas usefulPoint()
    {
        return this.usefulPoint;
    }

    public Atlas pointlessPointIntersectCorner()
    {
        return this.pointlessPointIntersectCorner;
    }

    public Atlas pointlessPointIntersect()
    {
        return this.pointlessPointIntersect;
    }

    public Atlas pointlessPointIntersectCornerDuplicate()
    {
        return this.pointlessPointIntersectCornerDuplicate;
    }

    public Atlas pointlessPointIntersectCornerDuplicateUseful()
    {
        return this.pointlessPointIntersectCornerDuplicateUseful;
    }

    public Atlas pointlessPointIntersectAreaCorner()
    {
        return this.pointlessPointIntersectAreaCorner;
    }

    public Atlas pointlessPointIntersectArea()
    {
        return this.pointlessPointIntersectArea;
    }
}
