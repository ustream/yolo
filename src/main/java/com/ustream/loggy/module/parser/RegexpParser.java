package com.ustream.loggy.module.parser;

import com.ustream.loggy.config.ConfigGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class RegexpParser implements IParser
{

    private Matcher matcher;

    private List<String> namedGroups = new ArrayList<String>();

    @Override
    public void setUp(Map<String, Object> parameters, boolean debug)
    {
        String regex = (String) parameters.get("regex");
        Pattern pattern = Pattern.compile(regex);
        matcher = pattern.matcher("");
        namedGroups = getNamedGroups(regex);
    }

    @Override
    public ConfigGroup getModuleConfig()
    {
        ConfigGroup config = new ConfigGroup();
        config.addConfigValue("regex", String.class);
        return config;
    }

    @Override
    public Map<String, String> parse(String line)
    {
        matcher.reset(line);

        if (matcher.find())
        {
            Map<String, String> result = new HashMap<String, String>();

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
    public List<String> getOutputParameters()
    {
        return namedGroups;
    }

    private List<String> getNamedGroups(String regex)
    {
        List<String> namedGroups = new ArrayList<String>();

        Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);

        while (m.find())
        {
            namedGroups.add(m.group(1));
        }

        return namedGroups;
    }

}
