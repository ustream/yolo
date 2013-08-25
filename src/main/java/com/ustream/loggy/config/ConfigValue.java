package com.ustream.loggy.config;

import java.util.Collection;
import java.util.List;
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

    private List<T> allowedValues = null;

    public ConfigValue(String name, Class<T> type)
    {
        this.name = name;
        this.type = type;
        required = true;
        defaultValue = null;
    }

    public ConfigValue(String name, Class<T> type, boolean required, T defaultValue)
    {
        this.name = name;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public void setAllowedValues(List<T> allowedValues)
    {
        this.allowedValues = allowedValues;
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
        if (required && isEmpty(value))
        {
            return false;
        }
        else if (value == null)
        {
            return true;
        }

        return type.isInstance(value) && (allowedValues == null || allowedValues.contains(value));
    }

    public String toString()
    {
        return String.format(
            "%s [%s]%s%s%s",
            name,
            type.getSimpleName(),
            required ? ", required" : "",
            !required && !isEmpty(defaultValue) ? ", default: " + defaultValue : "",
            allowedValues != null ? ", allowed values: " + allowedValues : ""
        );
    }
}
