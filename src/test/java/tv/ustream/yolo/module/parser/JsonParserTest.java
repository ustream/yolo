package tv.ustream.yolo.module.parser;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import tv.ustream.yolo.module.ModuleFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bandesz
 */
public class JsonParserTest
{

    private IParser parser;

    @Before
    public void setUp() throws Exception
    {
        parser = createParser();
    }

    @Test
    public void parseShouldProcessJsonAndReturnMap() throws Exception
    {
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");

        Map<String, String> actual = parser.parse("{\"key1\":\"value1\",\"key2\":\"value2\"}");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parseShouldFlattenMap() throws Exception
    {
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("key1.key2", "value1");
        expected.put("key3.key4", "value2");

        Map<String, String> actual = parser.parse(
                "{\"key1\":{\"key2\":\"value1\"},\"key3\":{\"key4\":\"value2\"}}"
        );

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parseShouldFlattenList() throws Exception
    {
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("key1.0.key2", "value1");
        expected.put("key1.1.key3", "value2");

        Map<String, String> actual = parser.parse("{\"key1\":[{\"key2\":\"value1\"},{\"key3\":\"value2\"}]}");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parseShouldHandleMixedStructure() throws Exception
    {
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("key1.key2", "value1");
        expected.put("key3.0", "value2");
        expected.put("key3.1", "value3");
        expected.put("key4", "value4");

        Map<String, String> actual = parser.parse(
                "{\"key1\":{\"key2\":\"value1\"},\"key3\":[\"value2\",\"value3\"], \"key4\":\"value4\"}"
        );

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parseShouldReturnNullWhenInvalidJson() throws Exception
    {
        Assert.assertNull(parser.parse(""));
        Assert.assertNull(parser.parse("{\"key\""));
    }

    private IParser createParser() throws Exception
    {
        Map<String, Object> processors = new HashMap<String, Object>();
        processors.put("processor1", new HashMap<String, Object>());

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", JsonParser.class.getCanonicalName());
        config.put("processors", processors);
        return new ModuleFactory().createParser("x", config);
    }

}
