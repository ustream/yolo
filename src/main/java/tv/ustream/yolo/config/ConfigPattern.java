package tv.ustream.yolo.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bandesz
 */
public class ConfigPattern
{

    private static final Pattern PARAMS_PATTERN = Pattern.compile("#([a-zA-Z0-9_\\-\\.]+)#");

    private final String pattern;

    private final List<String> parameters = new ArrayList<String>();

    private static Map<String, String> globalParameters = new HashMap<String, String>();

    private final boolean simplePattern;

    public ConfigPattern(final String pattern)
    {
        this.pattern = pattern;
        Matcher matcher = PARAMS_PATTERN.matcher(pattern);
        while (matcher.find())
        {
            parameters.add(matcher.group(1));
        }

        simplePattern = parameters.size() == 1 && pattern.startsWith("#") && pattern.endsWith("#");
    }

    public static boolean applicable(final Object pattern)
    {
        if (!(pattern instanceof String))
        {
            return false;
        }
        Matcher matcher = PARAMS_PATTERN.matcher((String) pattern);
        return matcher.find();
    }

    @SuppressWarnings("unchecked")
    public static Object replacePatterns(final Object data, final List<String> validKeys) throws ConfigException
    {
        if (data instanceof Map)
        {
            Map<String, Object> map = ((Map<String, Object>) data);
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                map.put(entry.getKey(), replacePatterns(entry.getValue(), validKeys));
            }
        }
        if (data instanceof List)
        {
            List<Object> list = (List<Object>) data;
            for (int i = 0; i < list.size(); i++)
            {
                list.set(i, replacePatterns(list.get(i), validKeys));
            }
        }
        if (applicable(data))
        {
            ConfigPattern pattern = new ConfigPattern((String) data);
            if (null != validKeys)
            {
                for (String key : pattern.getParameters())
                {
                    if (!validKeys.contains(key) && !globalParameters.containsKey(key))
                    {
                        throw new ConfigException("#" + key + "# parameter is missing from parser output!");
                    }
                }
            }
            return pattern;
        }
        else
        {
            return data;
        }
    }

    public String applyValues(final Map<String, Object> values)
    {
        if (simplePattern)
        {
            return (String) values.get(parameters.get(0));
        }
        else
        {
            String result = pattern;
            for (String parameter : parameters)
            {

                if (globalParameters.containsKey(parameter))
                {
                    result = result.replace("#" + parameter + "#", globalParameters.get(parameter));
                }
                else if (values.containsKey(parameter))
                {
                    result = result.replace("#" + parameter + "#", (String) values.get(parameter));
                }
                else
                {
                    return null;
                }
            }
            return result;
        }
    }

    public List<String> getParameters()
    {
        return parameters;
    }

    public static void addGlobalParameter(final String key, final String value)
    {
        globalParameters.put(key, value);
    }

    @Override
    public boolean equals(final Object o)
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

    @Override
    public String toString()
    {
        return "ConfigPattern('" + pattern + "')";
    }
}
