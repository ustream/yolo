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

    public void addConfigValue(ConfigValue configValue)
    {
        config.add(configValue);
    }

    public <T> void addConfigValue(String name, Class<T> type)
    {
        addConfigValue(name, type, true, null);
    }

    @SuppressWarnings("unchecked")
    public <T> void addConfigValue(String name, Class<T> type, boolean required, T defaultValue)
    {
        config.add(new ConfigValue(name, type, required, defaultValue));
    }

    public void merge(ConfigGroup configGroup)
    {
        if (null == configGroup) {
            return;
        }
        for (ConfigValue configValue : configGroup.config)
        {
            addConfigValue(configValue);
        }
    }

    public Map<String, Object> parseValues(String root, Map<String, Object> data) throws ConfigException
    {
        for (ConfigValue configValue : config)
        {
            Object value = data.get(configValue.getName());
            if (!configValue.validate(value))
            {
                throw new ConfigException(
                    root + "." + configValue.getName() + " field is missing or invalid, value definition: " + configValue + ""
                );
            }
            if (configValue.isEmpty(value))
            {
                data.put(configValue.getName(), configValue.getDefaultValue());
            }
        }
        return data;
    }

    public String getUsageString(String linePrefix)
    {
        String result = "";
        for (ConfigValue value : config)
        {
            result += linePrefix + value + "\n";
        }
        return result;
    }
}
