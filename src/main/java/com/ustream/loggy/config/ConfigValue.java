package com.ustream.loggy.config;

import java.util.Collection;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ConfigValue<T>
{

    private final String name;

    private final Class type;

    private final boolean required;

    private final T defaultValue;

    public ConfigValue(String name, Class<T> type, boolean required, T defaultValue)
    {
        this.name = name;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public String getName()
    {
        return name;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public boolean isEmpty(Object value)
    {
        if (value == null)
        {
            return true;
        }
        if (value instanceof String && "".equals(value))
        {
            return true;
        }
        if (value instanceof Collection && ((Collection) value).isEmpty())
        {
            return true;
        }

        if (value instanceof Map && ((Map) value).isEmpty())
        {
            return true;
        }

        return false;
    }

    public boolean validate(Object value)
    {
        if (required)
        {
            return !isEmpty(value) && type.isInstance(value);
        }
        else
        {
            return value == null || type.isInstance(value);
        }
    }

    public String toString()
    {
        return String.format(
            "%s [%s]%s%s",
            name,
            type.getSimpleName(),
            required ? ", required" : "",
            !required && !isEmpty(defaultValue) ? ", default: " + defaultValue : ""
        );
    }
}
