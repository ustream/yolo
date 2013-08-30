package tv.ustream.yolo.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bandesz
 */
public class ConfigMap implements IConfigEntry<Map<String, Object>>
{

    private final Map<String, IConfigEntry> config = new HashMap<String, IConfigEntry>();

    public void addConfigEntry(String name, IConfigEntry configValue)
    {
        config.put(name, configValue);
    }

    public <T> void addConfigValue(String name, Class<T> type)
    {
        addConfigValue(name, type, true, null);
    }

    @SuppressWarnings("unchecked")
    public <T> void addConfigValue(String name, Class<T> type, boolean required, T defaultValue)
    {
        config.put(name, new ConfigValue(type, required, defaultValue));
    }

    public void addConfigList(String name, ConfigMap configMap)
    {
        config.put(name, new ConfigList(configMap));
    }

    public ConfigMap merge(ConfigMap configMap)
    {
        if (null != configMap)
        {
            for (Map.Entry<String, IConfigEntry> configEntry : configMap.config.entrySet())
            {
                addConfigEntry(configEntry.getKey(), configEntry.getValue());
            }
        }
        return this;
    }

    public boolean isEmpty()
    {
        return config.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> parse(String name, Object data) throws ConfigException
    {
        if (!(data instanceof Map))
        {
            throw new ConfigException(name + " should be a map");
        }
        Map<String, Object> map = (Map<String, Object>) data;
        for (Map.Entry<String, IConfigEntry> configEntry : config.entrySet())
        {
            Object value = map.get(configEntry.getKey());
            map.put(configEntry.getKey(), configEntry.getValue().parse(name + "." + configEntry.getKey(), value));
        }
        return map;
    }

    public String getDescription(String indent)
    {
        String result = String.format("Map {%n");
        for (Map.Entry<String, IConfigEntry> configEntry : config.entrySet())
        {
            result += String.format("%s%s: %s", indent + "  ", configEntry.getKey(), configEntry.getValue().getDescription(indent + "  "));
        }
        result += String.format("%s}%n", indent);
        return result;
    }

}
