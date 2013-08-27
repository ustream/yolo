package tv.ustream.yolo.config;

import junit.framework.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author bandesz
 */
public class ConfigValueTest
{

    @Test
    public void isEmptyShouldReturnFalseWhenValueIsNotEmpty()
    {
        Assert.assertFalse(new ConfigValue<String>("key", String.class).isEmpty("value"));
    }

    @Test
    public void isEmptyShouldReturnTrueWhenValueIsNull()
    {
        Assert.assertTrue(new ConfigValue<String>("key", String.class).isEmpty(null));
    }

    @Test
    public void isEmptyShouldReturnTrueWhenValueIsEmptyString()
    {
        Assert.assertTrue(new ConfigValue<String>("key", String.class).isEmpty(""));
    }

    @Test
    public void isEmptyShouldReturnTrueWhenValueIsEmptyCollection()
    {
        Assert.assertTrue(new ConfigValue<Collection>("key", Collection.class).isEmpty(new ArrayList()));
    }

    @Test
    public void isEmptyShouldReturnTrueWhenValueIsEmptyMap()
    {
        Assert.assertTrue(new ConfigValue<Map>("key", Map.class).isEmpty(new HashMap()));
    }

    @Test
    public void validateShouldReturnTrueWhenRequiredAndNotEmpty()
    {
        Assert.assertTrue(new ConfigValue<String>("key", String.class).validateValue("value"));
    }

    @Test
    public void validateShouldReturnFalseWhenRequiredAndEmpty()
    {
        Assert.assertFalse(new ConfigValue<String>("key", String.class).validateValue(null));
    }

    @Test
    public void validateShouldReturnTrueWhenOptionalAndEmpty()
    {
        Assert.assertTrue(new ConfigValue<String>("key", String.class, false, null).validateValue(null));
    }

    @Test
    public void validateShouldReturnFalseWhenTypeIsInvalid()
    {
        Assert.assertFalse(new ConfigValue<String>("key", String.class).validateValue(new Object()));
    }

    @Test
    public void validateShouldReturnFalseWhenTypeIsNotInAllowedTypes()
    {
        ConfigValue value = new ConfigValue<Object>("key", Object.class);
        value.setAllowedTypes(Arrays.<Class>asList(String.class, Double.class));

        Assert.assertFalse(value.validateValue(new Object()));
    }

    @Test
    public void validateShouldReturnTrueWhenTypeIsInAllowedTypes()
    {
        ConfigValue value = new ConfigValue<Object>("key", Object.class);
        value.setAllowedTypes(Arrays.<Class>asList(String.class, Double.class));

        Assert.assertTrue(value.validateValue(5D));
    }
}
