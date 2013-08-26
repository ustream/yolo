package tv.ustream.loggy.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class ConfigGroup
{

    private final List<ConfigValue> configValues = new ArrayList<ConfigValue>();

    public void addConfigValue(ConfigValue configValue)
    {
        configValues.add(configValue);
    }

    public <T> void addConfigValue(String name, Class<T> type)
    {
        addConfigValue(name, type, true, null);
    }

    @SuppressWarnings("unchecked")
    public <T> void addConfigValue(String name, Class<T> type, boolean required, T defaultValue)
    {
        configValues.add(new ConfigValue(name, type, required, defaultValue));
    }

    public ConfigGroup merge(ConfigGroup configGroup)
    {
        if (null != configGroup)
        {
            for (ConfigValue configValue : configGroup.configValues)
            {
                addConfigValue(configValue);
            }
        }
        return this;
    }

    public Map<String, Object> parseValues(String root, Map<String, Object> data) throws ConfigException
    {
        for (ConfigValue configValue : configValues)
        {
            Object value = data.get(configValue.getName());
            if (!configValue.validateValue(value))
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
        for (ConfigValue value : configValues)
        {
            result += linePrefix + value + "\n";
        }
        return result;
    }

    public boolean isEmpty()
    {
        return configValues.isEmpty();
    }

}
