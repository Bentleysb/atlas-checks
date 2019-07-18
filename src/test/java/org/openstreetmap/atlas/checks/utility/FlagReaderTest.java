package org.openstreetmap.atlas.checks.utility;

import static org.openstreetmap.atlas.checks.utility.FlagReaderTestRule.CHECK_1;
import static org.openstreetmap.atlas.checks.utility.FlagReaderTestRule.CHECK_2;
import static org.openstreetmap.atlas.checks.utility.FlagReaderTestRule.IDENTIFIER_ONE;
import static org.openstreetmap.atlas.checks.utility.FlagReaderTestRule.IDENTIFIER_TWO;
import static org.openstreetmap.atlas.checks.utility.FlagReaderTestRule.TEST_INSTRUCTION;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Unit tests from {@link FlagReader} and {@link GeoJsonCheckFlag}.
 *
 * @author bbreithaupt
 */
public class FlagReaderTest
{

    @Rule
    public final FlagReaderTestRule setup = new FlagReaderTestRule();

    @Test
    public void oneFeatureTest()
    {
        final GeoJsonCheckFlag geoJsonCheckFlag = FlagReader
                .readFlagFromString(this.setup.getOneNodeCheckFlagEvent().toString());
        Assert.assertEquals(IDENTIFIER_ONE, geoJsonCheckFlag.getIdentifier());
        Assert.assertTrue(geoJsonCheckFlag.getInstructions().contains(TEST_INSTRUCTION));
        Assert.assertEquals(CHECK_1, geoJsonCheckFlag.getCheckName());
        Assert.assertEquals(1, geoJsonCheckFlag.getFeatures().size());
    }

    @Test
    public void reserializeTest()
    {
        final String flagString = this.setup.getTwoNodeCheckFlagEvent().toString();
        final GeoJsonCheckFlag geoJsonCheckFlag = FlagReader.readFlagFromString(flagString);
        Assert.assertEquals(flagString.length(), geoJsonCheckFlag.toString().length());
    }

    @Test
    public void twoFeaturesTest()
    {
        final GeoJsonCheckFlag geoJsonCheckFlag = FlagReader
                .readFlagFromString(this.setup.getTwoNodeCheckFlagEvent().toString());
        Assert.assertEquals(IDENTIFIER_ONE.concat(IDENTIFIER_TWO),
                geoJsonCheckFlag.getIdentifier());
        Assert.assertTrue(geoJsonCheckFlag.getInstructions().contains(TEST_INSTRUCTION));
        Assert.assertEquals(CHECK_2, geoJsonCheckFlag.getCheckName());
        Assert.assertEquals(2, geoJsonCheckFlag.getFeatures().size());
    }
}
