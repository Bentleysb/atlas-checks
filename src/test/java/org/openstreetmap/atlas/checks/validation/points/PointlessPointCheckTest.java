package org.openstreetmap.atlas.checks.validation.points;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Tests for {@link PointlessPointCheck}
 *
 * @author bbreithaupt
 */

public class PointlessPointCheckTest
{
    @Rule
    public PointlessPointCheckTestRule setup = new PointlessPointCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver.inlineConfiguration(
            "{\"PointlessPointCheck\":{\"pointless_tags.filter\":\"created_by->*|fixme->*\"}}");

    @Test
    public void pointlessPoint()
    {
        this.verifier.actual(this.setup.pointlessPoint(),
                new PointlessPointCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void usefulPoint()
    {
        this.verifier.actual(this.setup.usefulPoint(),
                new PointlessPointCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
