package com.ustream.loggy.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ConfigGroup
{

    private final List<ConfigValue> config = new ArrayList<ConfigValue>();

    public <T> void addConfigValue(String name, Class<T> type)
    {
        addConfigValue(name, type, true, null);
    }

    public <T> void addConfigValue(String name, Class<T> type, boolean required, T defaultValue)
    {
        config.add(new ConfigValue(name, type, required, defaultValue));
    }

    public void validate(String root, Map<String, Object> data) throws ConfigException
    {
        for (ConfigValue configValue : config)
        {
            if (!configValue.validate(data.get(configValue.getName())))
            {
                throw new ConfigException(
                    root + "." + configValue.getName() + " field is missing or invalid, value definition: " + configValue + ""
                );
            }
        }
    }

}
