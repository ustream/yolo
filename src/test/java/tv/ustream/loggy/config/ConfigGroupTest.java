package tv.ustream.loggy.config;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bandesz
 */
public class ConfigGroupTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void mergeShouldMergeTwoConfigGroups()
    {
        ConfigGroup cg1 = new ConfigGroup();
        cg1.addConfigValue("n1", String.class);

        ConfigGroup cg2 = new ConfigGroup();
        cg2.addConfigValue("n2", String.class);

        ConfigGroup expected = new ConfigGroup();
        expected.addConfigValue("n1", String.class);
        expected.addConfigValue("n2", String.class);

        Assert.assertEquals(expected.getUsageString(""), cg1.getUsageString("") + cg2.getUsageString(""));
    }

    @Test
    public void parseValuesShouldAcceptValidData() throws ConfigException
    {
        ConfigGroup cg = new ConfigGroup();
        cg.addConfigValue("n1", String.class);
        cg.addConfigValue("n2", String.class, false, null);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("n1", "almafa");

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("n1", "almafa");
        expected.put("n2", null);

        Assert.assertEquals(expected, cg.parseValues("root", data));
    }

    @Test
    public void parseValuesShouldThrowConfigExceptionWhenDataIsInvalid() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        ConfigGroup cg = new ConfigGroup();
        cg.addConfigValue("n1", String.class);
        cg.addConfigValue("n2", String.class, false, null);
        cg.addConfigValue("n3", Integer.class, false, 5);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("n2", "almafa");

        cg.parseValues("root", data);
    }

    @Test
    public void parseValuesShouldSetDefaultParameter() throws ConfigException
    {
        ConfigGroup cg = new ConfigGroup();
        cg.addConfigValue("n1", Integer.class, false, 5);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("n1", null);

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("n1", 5);

        Assert.assertEquals(expected, cg.parseValues("root", data));
    }

    @Test
    public void testAddConfigValueMethods() throws ConfigException
    {
        ConfigGroup cg = new ConfigGroup();
        cg.addConfigValue("n1", Integer.class);
        cg.addConfigValue("n2", Integer.class, false, 5);
        cg.addConfigValue(new ConfigValue<Integer>("n3", Integer.class, false, 10));

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("n1", 1);

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("n1", 1);
        expected.put("n2", 5);
        expected.put("n3", 10);

        Assert.assertEquals(expected, cg.parseValues("root", data));
    }

}
