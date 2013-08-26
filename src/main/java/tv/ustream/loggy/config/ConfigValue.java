package tv.ustream.loggy.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ConfigValue<T>
{

    private final String name;

    private final boolean required;

    private final T defaultValue;

    private List<T> allowedValues = null;

    private List<Class> allowedTypes = new ArrayList<Class>();

    public ConfigValue(String name, Class<T> type)
    {
        this(name, type, true, null);
    }

    public ConfigValue(String name, Class<T> type, boolean required, T defaultValue)
    {
        this.name = name;
        this.required = required;
        this.defaultValue = defaultValue;
        this.allowedTypes.add(type);
    }

    public void setAllowedValues(List<T> allowedValues)
    {
        this.allowedValues = allowedValues;
    }

    public void setAllowedTypes(List<Class> types)
    {
        this.allowedTypes = types;
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

        return isTypeAllowed(value) && (allowedValues == null || allowedValues.contains(value));
    }

    private boolean isTypeAllowed(Object value)
    {
        for (Class clazz : allowedTypes)
        {
            if (clazz.isInstance(value))
            {
                return true;
            }
        }
        return false;
    }

    public String toString()
    {
        String types = "";
        for (int i = 0; i < allowedTypes.size(); i++)
        {
            types = types + (i > 0 ? "|" : "") + allowedTypes.get(i).getSimpleName();
        }

        return String.format(
            "%s [%s]%s%s%s",
            name,
            types,
            required ? ", required" : "",
            !required && !isEmpty(defaultValue) ? ", default: " + defaultValue : "",
            allowedValues != null ? ", allowed values: " + allowedValues : ""
        );
    }
}
