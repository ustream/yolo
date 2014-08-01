package tv.ustream.yolo.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author bandesz
 */
public class ConfigPatternTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void replacePatternsShouldReplacePatternStringsWithObjects() throws ConfigException
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("key1", "simple string");
        data.put("key2", 5);
        data.put("key3", "string with #param#");
        data.put("key4", Arrays.<Object>asList("s1", "s1 #param#"));

        ConfigPattern.replacePatterns(data, null);

        Assert.assertEquals("simple string", data.get("key1"));
        Assert.assertEquals(5, data.get("key2"));
        Assert.assertEquals(new ConfigPattern("string with #param#"), data.get("key3"));
        Assert.assertEquals(Arrays.<Object>asList("s1", new ConfigPattern("s1 #param#")), data.get("key4"));
    }

    @Test
    public void replacePatternsShouldAllowValidKey() throws ConfigException
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("key1", "string with #param#");

        ConfigPattern.replacePatterns(data, Arrays.<String>asList("param"));

        Assert.assertEquals(new ConfigPattern("string with #param#"), data.get("key1"));
    }

    @Test
    public void replacePatternsShouldAllowGlobalKey() throws ConfigException
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("key1", "string with #GLOBAL_PARAM#");

        ConfigPattern.addGlobalParameter("GLOBAL_PARAM", "value");

        ConfigPattern.replacePatterns(data, Arrays.<String>asList("param"));

        Assert.assertEquals(new ConfigPattern("string with #GLOBAL_PARAM#"), data.get("key1"));
    }

    @Test
    public void replacePatternsShouldThrowExceptionWhenKeyIsInvalid() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("key1", "simple string");
        data.put("key2", 5);
        data.put("key3", "string with #param#");
        data.put("key4", Arrays.<Object>asList("s1", "s1 #param#"));

        ConfigPattern.replacePatterns(data, Arrays.<String>asList("paramOther"));

        Assert.assertEquals("simple string", data.get("key1"));
        Assert.assertEquals(5, data.get("key2"));
        Assert.assertEquals(new ConfigPattern("string with #param#"), data.get("key3"));
        Assert.assertEquals(Arrays.<Object>asList("s1", new ConfigPattern("s1 #param#")), data.get("key4"));
    }

    @Test
    public void applyValuesShouldReplaceParameters()
    {
        ConfigPattern pattern = new ConfigPattern("text #p1# text #p2# text");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p1", "v1");
        params.put("p2", "v2");
        String actual = pattern.applyValues(params);

        Assert.assertEquals("text v1 text v2 text", actual);
    }

    @Test
    public void applyValuesShouldReturnNullWhenParamsMissing()
    {
        ConfigPattern pattern = new ConfigPattern("text #p1# text #p2# text #p3# text");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p1", "v1");
        params.put("p2", "v2");
        String actual = pattern.applyValues(params);

        Assert.assertNull(actual);
    }

    @Test
    public void applyValuesShouldReturnValueForSimplePattern()
    {
        ConfigPattern pattern = new ConfigPattern("#p1#");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p1", "v1");
        String actual = pattern.applyValues(params);

        Assert.assertEquals("v1", actual);
    }

    @Test
    public void applyValuesShouldReturnNullForSimplePatternWhenValueMissing()
    {
        ConfigPattern pattern = new ConfigPattern("#p1#");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p2", "v1");
        String actual = pattern.applyValues(params);

        Assert.assertNull(actual);
    }
}
