package tv.ustream.loggy.module.processor;

import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.config.ConfigGroup;
import tv.ustream.loggy.config.ConfigPattern;
import tv.ustream.loggy.config.ConfigValue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class StatsDProcessor implements IProcessor
{

    private Logger logger = LoggerFactory.getLogger(StatsDProcessor.class);

    public static final String TYPE_COUNTER = "counter";

    public static final String TYPE_GAUGE = "gauge";

    public static final String TYPE_TIMER = "timer";

    private static final List<String> types = Arrays.asList(TYPE_COUNTER, TYPE_GAUGE, TYPE_TIMER);

    private static StatsDFactory statsDFactory = new StatsDFactory();

    private StatsDClient statsDClient;

    private boolean debug;

    @Override
    public void setUpModule(Map<String, Object> parameters, boolean debug)
    {
        this.debug = debug;

        String prefix = (String) parameters.get("prefix");
        String host = (String) parameters.get("host");
        Integer port = ((Number) parameters.get("port")).intValue();

        if (debug)
        {
            logger.info("Initializing StatsD connection: {}:{}", host, port);
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
    public ConfigGroup getProcessParamsConfig()
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
    public void validateProcessParams(List<String> parserOutputKeys, Map<String, Object> params) throws ConfigException
    {
        Object value = params.get("value");
        if (value instanceof String && !parserOutputKeys.contains(value))
        {
            throw new ConfigException("value parameter is missing from the parser output");
        }
    }

    @Override
    public void process(Map<String, String> parserOutput, Map<String, Object> processParams)
    {
        String type = (String) processParams.get("type");

        Object keyObject = processParams.get("key");
        String key;
        if (keyObject instanceof String)
        {
            key = (String) keyObject;
        }
        else
        {
            key = ((ConfigPattern) keyObject).applyValues(parserOutput);
        }

        Object valueObject = processParams.get("value");
        Double value;
        if (valueObject instanceof String)
        {
            value = Double.parseDouble(parserOutput.get(valueObject));
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
            logger.info("statsd: {} {} {}", type, key, String.valueOf(value));
        }

        if (TYPE_COUNTER.equals(type))
        {
            statsDClient.count(key, value);
        }
        else if (TYPE_GAUGE.equals(type))
        {
            statsDClient.gauge(key, value);
        }
        else if (TYPE_TIMER.equals(type))
        {
            statsDClient.time(key, value);
        }
    }

    public static void setStatsDFactory(StatsDFactory statsDFactory)
    {
        StatsDProcessor.statsDFactory = statsDFactory;
    }

}
