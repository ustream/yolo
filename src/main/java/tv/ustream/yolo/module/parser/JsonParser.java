package tv.ustream.yolo.module.parser;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tv.ustream.yolo.config.ConfigList;
import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.config.ConfigValue;

/**
 * @author bandesz
 */
public class JsonParser implements IParser
{

    private final Gson gson = new Gson();

    private boolean flatten;

    private final List<Filter> filters = new ArrayList<>();

    private boolean filtersEnabled = false;

    @Override
    public Map<String, Object> parse(final String line)
    {
        try
        {
            Map<String, Object> data = gson.fromJson(line, Map.class);
            if (data != null)
            {
                if (!flatten)
                {
                    return data;
                }
                else
                {
                    return filter(flattenMap("", data, new HashMap<String, Object>()));
                }
            }
            else
            {
                return null;
            }
        }
        catch (JsonParseException e)
        {
            return null;
        }
    }

    private Map<String, Object> flattenMap(final String path, final Object data, final Map<String, Object> result)
    {
        if (data instanceof Map)
        {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) data).entrySet())
            {
                flattenMap(path + "." + entry.getKey(), entry.getValue(), result);
            }
        }
        else if (data instanceof List)
        {
            int index = 0;
            for (Object element : ((List<Object>) data))
            {
                flattenMap(path + "." + index, element, result);
                index++;
            }
        }
        else
        {
            result.put(path.substring(1), data != null ? data.toString() : data);
        }

        return result;
    }

    private Map<String, Object> filter(final Map<String, Object> data)
    {
        if (!filtersEnabled)
        {
            return data;
        }

        for (Filter filter : filters)
        {
            if (data.containsKey(filter.getKey()) && filter.matchValue(data.get(filter.getKey())))
            {
                return data;
            }
        }
        return null;
    }

    @Override
    public boolean runAlways()
    {
        return false;
    }

    @Override
    public List<String> getOutputKeys()
    {
        return null;
    }

    @Override
    public void setUpModule(final Map<String, Object> parameters)
    {
        flatten = (Boolean) parameters.get("flatten");
        List<Object> rawFilters = (List<Object>) parameters.get("filters");
        for (Object rawFilter : rawFilters)
        {
            Map<String, Object> filter = (Map<String, Object>) rawFilter;
            filters.add(new Filter((String) filter.get("key"), filter.get("value")));
            filtersEnabled = true;
        }
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("flatten", Boolean.class, false, true);

        ConfigMap filterConfig = new ConfigMap();

        filterConfig.addConfigEntry("key", ConfigValue.createString());
        filterConfig.addConfigEntry("value", new ConfigValue<>(Object.class, false, Filter.NONE));

        ConfigList filters = new ConfigList(filterConfig, false, new ArrayList());

        config.addConfigEntry("filters", filters);

        return config;
    }

    @Override
    public String getModuleDescription()
    {
        return "parses JSON strings";
    }

    private static class Filter
    {

        public static final Object NONE = new Object();

        private String key;

        private Object value;

        private boolean valueFilterEnabled = false;

        private Filter(final String key, final Object value)
        {
            this.key = key;
            if (!NONE.equals(value))
            {
                this.value = value;
                valueFilterEnabled = true;
            }
        }

        public boolean matchValue(final Object value)
        {
            return !valueFilterEnabled || this.value.equals(value);
        }

        public String getKey()
        {
            return key;
        }
    }
}
