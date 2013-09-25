package tv.ustream.yolo.module.parser;

import tv.ustream.yolo.config.ConfigMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bandesz
 */
public class RegexpParser implements IParser
{

    private Matcher matcher;

    private final List<String> namedGroups = new ArrayList<String>();

    @Override
    public void setUpModule(final Map<String, Object> parameters)
    {
        String regex = (String) parameters.get("regex");
        matcher = Pattern.compile(regex).matcher("");
        setNamedGroups(regex);
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("regex", String.class);
        return config;
    }

    @Override
    public String getModuleDescription()
    {
        return "parses lines via regular expression and returns with matches";
    }

    @Override
    public Map<String, Object> parse(final String line)
    {
        matcher.reset(line);

        if (matcher.find())
        {
            Map<String, Object> result = new HashMap<String, Object>();

            for (String namedGroup : namedGroups)
            {
                try
                {
                    String value = matcher.group(namedGroup);
                    if (null != value)
                    {
                        result.put(namedGroup, value);
                    }
                }
                catch (IllegalArgumentException ignored)
                {
                }
            }

            return result;
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean runAlways()
    {
        return false;
    }

    @Override
    public List<String> getOutputKeys()
    {
        return namedGroups;
    }

    private void setNamedGroups(final String regex)
    {
        Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);

        while (m.find())
        {
            namedGroups.add(m.group(1));
        }
    }

}
