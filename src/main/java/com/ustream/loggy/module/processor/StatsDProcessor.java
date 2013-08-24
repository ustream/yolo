package com.ustream.loggy.module.processor;

import com.timgroup.statsd.StatsDClient;
import com.ustream.loggy.config.ConfigException;
import com.ustream.loggy.config.ConfigGroup;
import com.ustream.loggy.config.ConfigPattern;
import com.ustream.loggy.config.ConfigUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class StatsDProcessor implements IProcessor
{

    private static final List<String> types = Arrays.asList("count", "gauge", "time");

    private StatsDFactory statsDFactory = new StatsDFactory();

    private StatsDClient statsDClient;

    private boolean debug;

    @Override
    public void setUp(Map<String, Object> parameters, boolean debug)
    {
        this.debug = debug;

        String prefix = (String) parameters.get("prefix");
        String host = (String) parameters.get("host");
        Integer port = ConfigUtils.getInteger(parameters, "port", 8192);
        statsDClient = statsDFactory.createClient(prefix != null ? prefix : "", host, port);
    }

    @Override
    public ConfigGroup getModuleConfig()
    {
        ConfigGroup config = new ConfigGroup();
        config.addConfigValue("prefix", String.class, false, "");
        config.addConfigValue("host", String.class);
        config.addConfigValue("port", Double.class, false, 8192D);
        return config;
    }

    @Override
    public void validateProcessorParams(List<String> parserParams, Map<String, Object> params) throws ConfigException
    {
        String type = (String) params.get("type");
        if (!types.contains(type))
        {
            throw new ConfigException("type is invalid, must be count, gauge or time");
        }

        Object key = params.get("key");
        if (null == key || "".equals(key) || !(key instanceof String || key instanceof ConfigPattern))
        {
            throw new ConfigException("key parameter is missing from processor parameters");
        }

        Object value = params.get("value");
        if (null == value || "".equals(value) || !(value instanceof String || value instanceof Double))
        {
            throw new ConfigException("value parameter is missing from processor parameters");
        }
        if (value instanceof String && !parserParams.contains(value))
        {
            throw new ConfigException("value parameter is missing from the parser output");
        }
    }

    @Override
    public void process(Map<String, String> parserParams, Map<String, Object> processorParams)
    {
        String type = (String) processorParams.get("type");

        Object keyObject = processorParams.get("key");
        String key;
        if (keyObject instanceof String)
        {
            key = (String) keyObject;
        }
        else
        {
            key = ((ConfigPattern) keyObject).getValue(parserParams);
        }

        Object valueObject = processorParams.get("value");
        Double value;
        if (valueObject instanceof String)
        {
            value = Double.parseDouble(parserParams.get(valueObject));
        }
        else
        {
            value = (Double) valueObject;
        }

        send(type, key, value.intValue());
    }

    private void send(String type, String key, int value)
    {
        key = key.toLowerCase();

        if (debug)
        {
            System.out.println("statsd: " + type + " " + key + " " + String.valueOf(value));
        }

        if ("count".equals(type))
        {
            statsDClient.count(key, value);
        }
        else if ("gauge".equals(type))
        {
            statsDClient.gauge(key, value);
        }
        else if ("time".equals(type))
        {
            statsDClient.time(key, value);
        }
    }

    public void setStatsDFactory(StatsDFactory statsDFactory)
    {
        this.statsDFactory = statsDFactory;
    }

}
