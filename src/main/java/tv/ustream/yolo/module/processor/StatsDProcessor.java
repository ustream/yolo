package tv.ustream.yolo.module.processor;

import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.config.ConfigPattern;
import tv.ustream.yolo.config.ConfigValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class StatsDProcessor implements IProcessor
{

    private static final Logger logger = LoggerFactory.getLogger(StatsDProcessor.class);

    public static enum Types
    {
        COUNTER,
        GAUGE,
        TIMER;

        public final String value;

        private Types()
        {
            value = name().toLowerCase();
        }

        public static List<String> getStringValues()
        {
            List<String> values = new ArrayList<String>();
            for (Types type : Types.values())
            {
                values.add(type.value);
            }
            return values;
        }

    }

    private static StatsDFactory statsDFactory = new StatsDFactory();

    private StatsDClient statsDClient;

    @Override
    public void setUpModule(Map<String, Object> parameters)
    {
        String prefix = (String) parameters.get("prefix");
        String host = (String) parameters.get("host");
        Integer port = ((Number) parameters.get("port")).intValue();

        logger.debug("Initializing StatsD connection: {}:{}", host, port);

        statsDClient = statsDFactory.createClient(prefix != null ? prefix : "", host, port);
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("prefix", String.class);
        config.addConfigValue("host", String.class);
        config.addConfigValue("port", Number.class, false, 8125);
        return config;
    }

    @Override
    public ConfigMap getProcessParamsConfig()
    {
        ConfigMap config = new ConfigMap();

        ConfigValue<String> typeConfig = new ConfigValue<String>(String.class);
        typeConfig.setAllowedValues(Types.getStringValues());
        config.addConfigEntry("type", typeConfig);

        config.addConfigValue("key", String.class);

        ConfigValue<Object> valueConfig = new ConfigValue<Object>(Object.class);
        valueConfig.setAllowedTypes(Arrays.<Class>asList(String.class, Number.class));
        config.addConfigEntry("value", valueConfig);

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

        logger.debug("statsd: {} {} {}", type, key, String.valueOf(value));

        if (Types.COUNTER.value.equals(type))
        {
            statsDClient.count(key, value);
        }
        else if (Types.GAUGE.value.equals(type))
        {
            statsDClient.gauge(key, value);
        }
        else if (Types.TIMER.value.equals(type))
        {
            statsDClient.time(key, value);
        }
    }

    public static void setStatsDFactory(StatsDFactory statsDFactory)
    {
        StatsDProcessor.statsDFactory = statsDFactory;
    }

}
