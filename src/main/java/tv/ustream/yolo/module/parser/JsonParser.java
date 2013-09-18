package tv.ustream.yolo.module.parser;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import tv.ustream.yolo.config.ConfigMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class JsonParser implements IParser
{

    private final Gson gson = new Gson();

    @Override
    public Map<String, String> parse(final String line)
    {
        try
        {
            Map<String, Object> data = gson.fromJson(line, Map.class);
            if (data != null)
            {
                return flattenMap("", data, new HashMap<String, String>());
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

    private Map<String, String> flattenMap(final String path, final Object data, final Map<String, String> result)
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
            result.put(path.substring(1), data.toString());
        }

        return result;
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
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        return null;
    }

    @Override
    public String getModuleDescription()
    {
        return "parses JSON strings and returns with flatten map ('level1.level2' -> 'value')";
    }
}
