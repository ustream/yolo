package com.ustream.loggy.config;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ConfigUtils
{

    public static Map<String, Object> getConfigFromFile(String configPath) throws IOException
    {
        Gson gson = new Gson();
        return (Map<String, Object>) gson.fromJson(new FileReader(configPath), Map.class);
    }

    public static Map<String, Object> getObjectMap(Object data, String key)
    {
        if (!(data instanceof Map))
        {
            throw new IllegalArgumentException("Can not read " + key + " value, maybe it is missing");
        }
        try
        {
            return (Map<String, Object>) ((Map<String, Object>) data).get(key);
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException(key + " value should be a map");
        }
    }

    public static Map<String, Object> castObjectMap(Object data)
    {
        try
        {
            return (Map<String, Object>) data;
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("value should be a map");
        }
    }

    public static Integer getInteger(Map<String, Object> data, String key, Integer defaultValue)
    {
        Object value = data.get(key);
        if (null != value)
        {
            if (value instanceof Double)
            {
                return ((Double) value).intValue();
            }
            else
            {
                throw new IllegalArgumentException(key + " value should be numeric");
            }
        }
        else
        {
            return defaultValue;
        }
    }

}
