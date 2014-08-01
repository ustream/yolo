package tv.ustream.yolo.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author bandesz
 */
public class ConfigValueTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parseShouldReturnValueWhenValueIsNotEmpty() throws ConfigException
    {
        Assert.assertEquals("value", new ConfigValue<String>(String.class).parse("root", "value"));
    }

    @Test
    public void parseShouldThrowExceptionWhenRequiredAndValueIsNull() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        new ConfigValue<String>(String.class).parse("root", null);
    }

    @Test
    public void parseShouldThrowExceptionWhenRequiredAndValueIsEmptyString() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        new ConfigValue<String>(String.class).parse("root", "");
    }

    @Test
    public void parseShouldThrowExceptionWhenRequiredAndValueIsEmptyCollection() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        new ConfigValue<Collection>(Collection.class).parse("root", new ArrayList());
    }

    @Test
    public void parseShouldThrowExceptionWhenRequiredAndValueIsEmptyMap() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        new ConfigValue<Map>(Map.class).parse("root", new HashMap());
    }

    @Test
    public void parseShouldReturnEmptyWhenOptionalAndEmpty() throws ConfigException
    {
        Assert.assertNull(new ConfigValue<String>(String.class, false, null).parse("root", null));
    }

    @Test
    public void parseShouldThrowExceptionWhenTypeIsInvalid() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        new ConfigValue<String>(String.class).parse("root", new Object());
    }

    @Test
    public void parseShouldThrowExceptionWhenTypeIsNotInAllowedTypes() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        ConfigValue value = new ConfigValue<Object>(Object.class);
        value.setAllowedTypes(Arrays.<Class>asList(String.class, Double.class));

        value.parse("root", new Object());
    }

    @Test
    public void parseShouldReturnValueWhenTypeIsInAllowedTypes() throws ConfigException
    {
        ConfigValue value = new ConfigValue<Object>(Object.class);
        value.setAllowedTypes(Arrays.<Class>asList(String.class, Double.class));

        Assert.assertEquals(5D, value.parse("root", 5D));
    }

    @Test
    public void parseShouldThrowExceptionWhenTypeIsNotInAllowedValues() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        ConfigValue<String> value = new ConfigValue<String>(String.class);
        value.setAllowedValues(Arrays.<String>asList("a", "b", "c"));

        value.parse("root", "d");
    }

    @Test
    public void parseShouldReturnValueWhenTypeIsInAllowedValues() throws ConfigException
    {
        ConfigValue<String> value = new ConfigValue<String>(String.class);
        value.setAllowedValues(Arrays.<String>asList("a", "b", "c"));

        Assert.assertEquals("c", value.parse("root", "c"));
    }

    @Test
    public void parseShouldAllowPatternWhenAllowed() throws ConfigException
    {
        ConfigValue<String> value = new ConfigValue<String>(String.class);
        value.allowConfigPattern();

        Assert.assertEquals("xxx #param# xxx", value.parse("root", "xxx #param# xxx"));
    }


    @Test
    public void parseShouldThrowExceptionWhenPatternIsNotAllowed() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        ConfigValue<String> value = new ConfigValue<String>(String.class);

        value.parse("root", "xxx #param# xxx");
    }

}
