package tv.ustream.yolo.module.processor;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.config.ConfigPattern;
import tv.ustream.yolo.config.ConfigValue;
import tv.ustream.yolo.util.NumberConverter;

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

    private StatsDClient statsDClient;

    protected StatsDClient createClient(String prefix, String host, int port)
    {
        return new NonBlockingStatsDClient(prefix, host, port);
    }

    @Override
    public void setUpModule(Map<String, Object> parameters)
    {
        String prefix = (String) parameters.get("prefix");
        String host = (String) parameters.get("host");
        Integer port = ((Number) parameters.get("port")).intValue();

        logger.debug("Initializing StatsD connection: {}:{}", host, port);

        statsDClient = createClient(prefix != null ? prefix : "", host, port);
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
        ConfigMap map = new ConfigMap();

        ConfigMap keyConfig = new ConfigMap();

        keyConfig.addConfigEntry("type", ConfigValue.createString().setAllowedValues(Types.getStringValues()));
        keyConfig.addConfigEntry("key", ConfigValue.createString().allowConfigPattern());

        ConfigValue<Object> valueConfig = new ConfigValue<Object>(Object.class);
        valueConfig.setAllowedTypes(Arrays.<Class>asList(String.class, Number.class));
        valueConfig.allowConfigPattern();
        keyConfig.addConfigEntry("value", valueConfig);

        keyConfig.addConfigValue("multiplier", Number.class, false, 1);

        map.addConfigList("keys", keyConfig);

        return map;
    }

    @Override
    public String getModuleDescription()
    {
        return "sends metrics to StatsD, handles counter, gauge and timing values";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(Map<String, String> parserOutput, Map<String, Object> processParams)
    {
        List<Map<String, Object>> keys = (List<Map<String, Object>>) processParams.get("keys");

        for (Map<String, Object> keyParams : keys)
        {
            sendKey(parserOutput, keyParams);
        }
    }

    private void sendKey(Map<String, String> parserOutput, Map<String, Object> keyParams)
    {
        String type = (String) keyParams.get("type");

        Object keyObject = keyParams.get("key");
        String key;
        if (keyObject instanceof String)
        {
            key = (String) keyObject;
        }
        else
        {
            key = ((ConfigPattern) keyObject).applyValues(parserOutput);
        }

        Object valueObject = keyParams.get("value");
        Double value;
        if (valueObject instanceof Number)
        {
            value = ((Number) valueObject).doubleValue();

        }
        else
        {
            value = NumberConverter.convertByteValue(((ConfigPattern) valueObject).applyValues(parserOutput));
        }

        value *= ((Number) keyParams.get("multiplier")).doubleValue();

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

    @Override
    public void stop()
    {
    }

}
