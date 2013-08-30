package tv.ustream.yolo.config;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bandesz
 */
public class ConfigPatternTest
{

    @Test
    public void replacePatternsShouldReplacePatternStringsWithObjects()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("key1", "simple string");
        data.put("key2", 5);
        data.put("key3", "string with #param#");
        data.put("key4", Arrays.<Object>asList("s1", "s1 #param#"));

        ConfigPattern.replacePatterns(data);

        Assert.assertEquals("simple string", data.get("key1"));
        Assert.assertEquals(5, data.get("key2"));
        Assert.assertEquals(new ConfigPattern("string with #param#"), data.get("key3"));
        Assert.assertEquals(Arrays.<Object>asList("s1", new ConfigPattern("s1 #param#")), data.get("key4"));
    }

    @Test
    public void applyValuesShouldReplaceParameters()
    {
        ConfigPattern pattern = new ConfigPattern("text #p1# text #p2# text");
        Map<String, String> params = new HashMap<String, String>();
        params.put("p1", "v1");
        params.put("p2", "v2");
        String actual = pattern.applyValues(params);

        Assert.assertEquals("text v1 text v2 text", actual);
    }

    @Test
    public void applyValuesShouldLeaveMissingParamAsIs()
    {
        ConfigPattern pattern = new ConfigPattern("text #p1# text #p2# text #p3# text");
        Map<String, String> params = new HashMap<String, String>();
        params.put("p1", "v1");
        params.put("p2", "v2");
        String actual = pattern.applyValues(params);

        Assert.assertEquals("text v1 text v2 text #p3# text", actual);
    }
}
