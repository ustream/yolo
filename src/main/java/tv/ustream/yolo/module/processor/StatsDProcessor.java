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

    private static final Logger LOG = LoggerFactory.getLogger(StatsDProcessor.class);

    private static final int DEFAULT_PORT = 8125;

    public static enum Types
    {
        COUNTER,
        GAUGE,
        TIMER;

        private final String value;

        private Types()
        {
            value = name().toLowerCase();
        }

        public static List<String> getStringValues()
        {
            List<String> values = new ArrayList<String>();
            for (Types type : Types.values())
            {
                values.add(type.getValue());
            }
            return values;
        }

        public String getValue()
        {
            return value;
        }
    }

    private StatsDClient statsDClient;

    protected StatsDClient createClient(final String prefix, final String host, final int port)
    {
        return new NonBlockingStatsDClient(prefix, host, port);
    }

    @Override
    public void setUpModule(final Map<String, Object> parameters)
    {
        String prefix = (String) parameters.get("prefix");
        if (null == prefix)
        {
            prefix = "";
        }

        String host = (String) parameters.get("host");
        Integer port = ((Number) parameters.get("port")).intValue();

        LOG.debug("Initializing StatsD connection: {}:{}", host, port);

        statsDClient = createClient(prefix, host, port);
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("prefix", String.class);
        config.addConfigValue("host", String.class);
        config.addConfigValue("port", Number.class, false, DEFAULT_PORT);
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
    public void process(final Map<String, String> parserOutput, final Map<String, Object> processParams)
    {
        List<Map<String, Object>> keys = (List<Map<String, Object>>) processParams.get("keys");

        for (Map<String, Object> keyParams : keys)
        {
            sendKey(parserOutput, keyParams);
        }
    }

    private void sendKey(final Map<String, String> parserOutput, final Map<String, Object> keyParams)
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

    private void send(final String type, final String key, final int value)
    {
        LOG.debug("statsd: {} {} {}", type, key, String.valueOf(value));

        if (Types.COUNTER.getValue().equals(type))
        {
            statsDClient.count(key, value);
        }
        else if (Types.GAUGE.getValue().equals(type))
        {
            statsDClient.gauge(key, value);
        }
        else if (Types.TIMER.getValue().equals(type))
        {
            statsDClient.time(key, value);
        }
    }

    @Override
    public void stop()
    {
    }

}
