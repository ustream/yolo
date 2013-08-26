package tv.ustream.loggy.config;

import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bandesz
 */
public class ConfigPatternTest
{

    @Test
    public void isPatternShouldReturnFalseForNotString() throws Exception
    {
        Assert.assertFalse(ConfigPattern.isPattern(5));
    }

    @Test
    public void isPatternShouldReturnFalseForSimpleString() throws Exception
    {
        Assert.assertFalse(ConfigPattern.isPattern("simple string #notparam"));
    }

    @Test
    public void isPatternShouldReturnTrueForPatternString() throws Exception
    {
        Assert.assertTrue(ConfigPattern.isPattern("string with #param#"));
    }

    @Test
    public void processMapShouldReplacePatternStringsWithObjects() throws Exception
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("key1", "simple string");
        data.put("key2", 5);
        data.put("key3", "string with #param#");

        ConfigPattern.processMap(data);

        Assert.assertEquals("simple string", data.get("key1"));
        Assert.assertEquals(5, data.get("key2"));
        Assert.assertEquals(new ConfigPattern("string with #param#"), data.get("key3"));
    }

    @Test
    public void getValueShouldReplaceParameters() throws Exception
    {
        ConfigPattern pattern = new ConfigPattern("text #p1# text #p2# text");
        Map<String, String> params = new HashMap<String, String>();
        params.put("p1", "v1");
        params.put("p2", "v2");
        String actual = pattern.getValue(params);

        Assert.assertEquals("text v1 text v2 text", actual);
    }

    @Test
    public void getValueShouldLeaveMissingParamAsIs() throws Exception
    {
        ConfigPattern pattern = new ConfigPattern("text #p1# text #p2# text #p3# text");
        Map<String, String> params = new HashMap<String, String>();
        params.put("p1", "v1");
        params.put("p2", "v2");
        String actual = pattern.getValue(params);

        Assert.assertEquals("text v1 text v2 text #p3# text", actual);
    }
}
