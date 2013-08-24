package com.ustream.loggy.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ConfigPattern
{

    private static final Pattern parametersPattern = Pattern.compile("#([a-zA-Z0-9_-]+)#");

    private final String pattern;

    private final List<String> parameters = new ArrayList<String>();

    public ConfigPattern(String pattern)
    {
        this.pattern = pattern;
        Matcher matcher = parametersPattern.matcher(pattern);
        while (matcher.find())
        {
            parameters.add(matcher.group(1));
        }
    }

    public static boolean isPattern(Object pattern)
    {
        if (!(pattern instanceof String))
        {
            return false;
        }
        Matcher matcher = parametersPattern.matcher((String) pattern);
        return matcher.find();
    }

    public static Map<String, Object> processMap(Map<String, Object> data)
    {
        for (String key : data.keySet())
        {
            if (isPattern(data.get(key)))
            {
                data.put(key, new ConfigPattern((String) data.get(key)));
            }
        }
        return data;
    }

    public String getValue(Map<String, String> values)
    {
        String result = pattern;
        for (String parameter : parameters)
        {
            if (values.containsKey(parameter))
            {
                result = result.replace("#" + parameter + "#", values.get(parameter));
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ConfigPattern that = (ConfigPattern) o;

        return pattern.equals(that.pattern);

    }

    @Override
    public int hashCode()
    {
        return pattern.hashCode();
    }
}
