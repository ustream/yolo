package tv.ustream.yolo.module.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tv.ustream.yolo.module.ModuleFactory;

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
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");

        Map<String, Object> actual = parser.parse("{\"key1\":\"value1\",\"key2\":\"value2\"}");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parseShouldFlattenMap() throws Exception
    {
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("key1.key2", "value1");
        expected.put("key3.key4", "value2");

        Map<String, Object> actual = parser.parse(
                "{\"key1\":{\"key2\":\"value1\"},\"key3\":{\"key4\":\"value2\"}}"
        );

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parseShouldFlattenList() throws Exception
    {
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("key1.0.key2", "value1");
        expected.put("key1.1.key3", "value2");

        Map<String, Object> actual = parser.parse("{\"key1\":[{\"key2\":\"value1\"},{\"key3\":\"value2\"}]}");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parseShouldHandleMixedStructure() throws Exception
    {
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("key1.key2", "value1");
        expected.put("key3.0", "value2");
        expected.put("key3.1", "value3");
        expected.put("key4", "value4");

        Map<String, Object> actual = parser.parse(
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

    @Test
    public void parseShouldReturnMapWhenFlattenDisabled() throws Exception
    {
        parser = createParser(false);

        Map<String, Object> entry = new HashMap<String, Object>();
        entry.put("key3", "value3");

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("key1", "value1");
        expected.put("key2", entry);

        Map<String, Object> actual = parser.parse("{\"key1\":\"value1\",\"key2\":{\"key3\":\"value3\"}}");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parserShouldAllowWithKeyFilter() throws Exception
    {
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("key1", null);
        expected.put("key3", "x");

        parser = createParserWithFilter();

        Map<String, Object> actual = parser.parse(
            "{\"key1\":null,\"key3\":\"x\"}"
        );

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parserShouldAllowWithKeyValueFilter() throws Exception
    {
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("key2", "value2");
        expected.put("key3", "x");

        parser = createParserWithFilter();

        Map<String, Object> actual = parser.parse(
            "{\"key2\":\"value2\",\"key3\":\"x\"}"
        );

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parserShouldDenyWhenKeyDoesNotExist() throws Exception
    {
        parser = createParserWithFilter();

        Map<String, Object> actual = parser.parse(
            "{\"key2\":null,\"key3\":\"y\"}"
        );

        Assert.assertNull(actual);
    }

    @Test
    public void parserShouldDenyWhenValueIsDifferent() throws Exception
    {
        parser = createParserWithFilter();

        Map<String, Object> actual = parser.parse(
            "{\"key3\":\"x\",\"key4\":\"y\"}"
        );

        Assert.assertNull(actual);
    }

    private IParser createParser() throws Exception
    {
        return createParser(true);
    }

    private IParser createParser(final boolean flatten) throws Exception
    {
        Map<String, Object> processors = new HashMap<String, Object>();
        processors.put("processor1", new HashMap<String, Object>());

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", JsonParser.class.getCanonicalName());
        config.put("processors", processors);
        config.put("flatten", flatten);
        return new ModuleFactory().createParser("x", config);
    }

    private IParser createParserWithFilter() throws Exception
    {
        Map<String, Object> processors = new HashMap<String, Object>();
        processors.put("processor1", new HashMap<String, Object>());

        Map<String, Object> filter1 = new HashMap<>();
        filter1.put("key", "key1");

        Map<String, Object> filter2 = new HashMap<>();
        filter2.put("key", "key2");
        filter2.put("value", "value2");

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", JsonParser.class.getCanonicalName());
        config.put("processors", processors);
        config.put("flatten", true);
        config.put("filters", Arrays.asList(filter1, filter2));
        return new ModuleFactory().createParser("x", config);
    }

}
