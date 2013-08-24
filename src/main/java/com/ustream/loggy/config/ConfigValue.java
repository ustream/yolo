package com.ustream.loggy.config;

import java.util.Collection;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ConfigValue<T>
{

    private String name;

    private Class type;

    private boolean required = true;

    private T defaultValue = null;

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

    public boolean validate(Object value)
    {
        if (required)
        {
            if (value == null)
            {
                return false;
            }
            if (value instanceof Collection && ((Collection) value).isEmpty())
            {
                return false;
            }

            if (value instanceof Map && ((Map) value).isEmpty())
            {
                return false;
            }
            return true;
        }

        return value == null || type.isInstance(value);
    }

    public String toString()
    {
        return String.format(
            "%s[%s]%s%s",
            name,
            type.getName(),
            required ? "+" : "",
            !required && defaultValue != null ? " (default: " + defaultValue + ")" : ""
        );
    }
}
