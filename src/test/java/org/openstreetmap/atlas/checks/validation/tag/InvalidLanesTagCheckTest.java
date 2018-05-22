package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link InvalidLanesTagCheck}
 *
 * @author bbreithaupt
 */
public class InvalidLanesTagCheckTest
{
    @Rule
    public InvalidLanesTagCheckTestRule setup = new InvalidLanesTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void validLanesTag()
    {
        this.verifier.actual(this.setup.validLanesTag(),
                new InvalidLanesTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidLanesTagCheck\":{\"lanes.filter\":\"lanes->1,1.5,2\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidLanesTag()
    {
        this.verifier.actual(this.setup.invalidLanesTag(),
                new InvalidLanesTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidLanesTagCheck\":{\"lanes.filter\":\"lanes->1,1.5,2\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}
