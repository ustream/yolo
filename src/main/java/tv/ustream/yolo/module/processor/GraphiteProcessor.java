package tv.ustream.yolo.module.processor;

import tv.ustream.yolo.client.GraphiteClient;
import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.config.ConfigPattern;
import tv.ustream.yolo.config.ConfigValue;
import tv.ustream.yolo.util.NumberConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class GraphiteProcessor implements IProcessor
{

    private static final int DEFAULT_PORT = 2003;

    private static final long DEFAULT_FLUSH_TIME_MS = 1000;

    private GraphiteClient client;

    protected GraphiteClient createClient(final String host, final int port, final long flushTimeMs)
    {
        return new GraphiteClient(host, port, flushTimeMs);
    }

    @Override
    public ConfigMap getProcessParamsConfig()
    {
        ConfigMap map = new ConfigMap();

        ConfigMap keyConfig = new ConfigMap();

        keyConfig.addConfigEntry("key", ConfigValue.createString().allowConfigPattern());

        ConfigValue<Object> valueConfig = new ConfigValue<Object>(Object.class);
        valueConfig.setAllowedTypes(Arrays.<Class>asList(String.class, Number.class));
        valueConfig.allowConfigPattern();
        keyConfig.addConfigEntry("value", valueConfig);

        keyConfig.addConfigValue("multiplier", Number.class, false, 1);

        ConfigValue timestampConfig = new ConfigValue<String>(String.class, false, null);
        timestampConfig.allowConfigPattern();
        keyConfig.addConfigEntry("timestamp", timestampConfig);

        map.addConfigList("keys", keyConfig);

        return map;
    }

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

        Object timestampObject = keyParams.get("timestamp");

        if (timestampObject instanceof ConfigPattern)
        {
            long timestamp = Long.parseLong(((ConfigPattern) timestampObject).applyValues(parserOutput));
            client.sendMetrics(key, value, timestamp);
        }
        else
        {
            client.sendMetrics(key, value);
        }
    }

    @Override
    public void setUpModule(final Map<String, Object> parameters)
    {
        String host = (String) parameters.get("host");
        Integer port = ((Number) parameters.get("port")).intValue();
        Long flushTimeMs = ((Number) parameters.get("flushTimeMs")).longValue();

        client = createClient(host, port, flushTimeMs);
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("host", String.class);
        config.addConfigValue("port", Number.class, false, DEFAULT_PORT);
        config.addConfigValue("flushTimeMs", Number.class, false, DEFAULT_FLUSH_TIME_MS);
        return config;
    }

    @Override
    public String getModuleDescription()
    {
        return "sends metrics to Graphite";
    }

    @Override
    public void stop()
    {
        client.stop();
    }

}
