package com.ustream.loggy.config;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ConfigUtils
{

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getConfigFromFile(String configPath) throws IOException
    {
        Gson gson = new Gson();
        return (Map<String, Object>) gson.fromJson(new FileReader(configPath), Map.class);
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    public static List<String> castStringList(Object data)
    {
        try
        {
            return (List<String>) data;
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("value should be a string array");
        }
    }

    public static Integer getInteger(Map<String, Object> data, String key)
    {
        Object value = data.get(key);
        if (null != value)
        {
            if (value instanceof Number)
            {
                return ((Number) value).intValue();
            }
            else
            {
                throw new IllegalArgumentException(key + " value should be numeric");
            }
        }
        else
        {
            return null;
        }
    }

}
