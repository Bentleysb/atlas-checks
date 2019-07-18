package org.openstreetmap.atlas.checks.utility;

import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Unit test rule for {@link FlagReaderTest}.
 *
 * @author bbreithaupt
 */
public class FlagReaderTestRule extends CoreTestRule
{
    static final String TEST_INSTRUCTION = "This is a test flag.";
    static final String IDENTIFIER_ONE = "1000000";
    static final String IDENTIFIER_TWO = "2000000";
    static final String CHECK_1 = "Check1";
    static final String CHECK_2 = "Check2";

    private static final String TEST_1 = "42.7856273,10.2804429";
    private static final String TEST_2 = "42.8168030,10.3320820";

    @TestAtlas(nodes = { @Node(id = IDENTIFIER_ONE, coordinates = @Loc(value = TEST_1)),
            @Node(id = IDENTIFIER_TWO, coordinates = @Loc(value = TEST_2)) })
    private Atlas atlas;

    /**
     * Generate a simple {@link CheckFlagEvent}, that has one node and uses a test instruction.
     *
     * @return a {@link CheckFlagEvent}
     */
    public CheckFlagEvent getOneNodeCheckFlagEvent()
    {
        final AtlasObject node = this.atlas.node(1000000L);
        final CheckFlag flag = new CheckFlag(String.valueOf(node.getIdentifier()));
        flag.addObject(node);
        flag.addInstruction(TEST_INSTRUCTION);

        return new CheckFlagEvent(CHECK_1, flag);
    }

    /**
     * Generate a simple {@link CheckFlagEvent}, that has two nodes and uses a test instruction.
     *
     * @return a {@link CheckFlagEvent}
     */
    public CheckFlagEvent getTwoNodeCheckFlagEvent()
    {
        final AtlasObject nodeOne = this.atlas.node(1000000L);
        final AtlasObject nodeTwo = this.atlas.node(2000000L);
        final CheckFlag flag = new CheckFlag(IDENTIFIER_ONE.concat(IDENTIFIER_TWO));
        flag.addObject(nodeOne);
        flag.addObject(nodeTwo);
        flag.addInstruction(TEST_INSTRUCTION);

        return new CheckFlagEvent(CHECK_2, flag);
    }
}
