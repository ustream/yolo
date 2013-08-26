package tv.ustream.loggy.module.processor;

import com.timgroup.statsd.StatsDClient;
import tv.ustream.loggy.config.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class StatsDProcessor implements IProcessor
{

    private static final List<String> types = Arrays.asList("count", "gauge", "time");

    private static StatsDFactory statsDFactory = new StatsDFactory();

    private StatsDClient statsDClient;

    private boolean debug;

    @Override
    public void setUpModule(Map<String, Object> parameters, boolean debug)
    {
        this.debug = debug;

        String prefix = (String) parameters.get("prefix");
        String host = (String) parameters.get("host");
        Integer port = ConfigUtils.getInteger(parameters, "port");

        if (debug)
        {
            System.out.format("Initializing StatsD connection: %s:%d\n", host, port);
        }

        statsDClient = statsDFactory.createClient(prefix != null ? prefix : "", host, port);
    }

    @Override
    public ConfigGroup getModuleConfig()
    {
        ConfigGroup config = new ConfigGroup();
        config.addConfigValue("prefix", String.class, false, "");
        config.addConfigValue("host", String.class);
        config.addConfigValue("port", Number.class, false, 8192);
        return config;
    }

    @Override
    public ConfigGroup getProcessorParamsConfig()
    {
        ConfigGroup config = new ConfigGroup();

        ConfigValue<String> typeConfig = new ConfigValue<String>("type", String.class);
        typeConfig.setAllowedValues(types);
        config.addConfigValue(typeConfig);

        config.addConfigValue("key", String.class);

        ConfigValue<Object> valueConfig = new ConfigValue<Object>("value", Object.class);
        valueConfig.setAllowedTypes(Arrays.<Class>asList(String.class, Number.class));
        config.addConfigValue(valueConfig);

        return config;
    }

    @Override
    public String getModuleDescription()
    {
        return "sends metrics to statsd, it handles counter, gauge and timing values";
    }

    @Override
    public void validateProcessorParams(List<String> parserParams, Map<String, Object> params) throws ConfigException
    {
        Object key = params.get("key");
        if (!(key instanceof String) && !(key instanceof ConfigPattern))
        {
            throw new ConfigException("key parameter is invalid!");
        }

        Object value = params.get("value");
        if (!(value instanceof String) && !(value instanceof Double))
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

    public static void setStatsDFactory(StatsDFactory statsDFactory)
    {
        StatsDProcessor.statsDFactory = statsDFactory;
    }

}
