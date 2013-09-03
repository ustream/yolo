package tv.ustream.yolo.module.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.client.GraphiteClient;
import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.config.ConfigPattern;
import tv.ustream.yolo.config.ConfigValue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class GraphiteProcessor implements IProcessor
{

    private static final Logger logger = LoggerFactory.getLogger(GraphiteProcessor.class);

    public static GraphiteFactory graphiteFactory = new GraphiteFactory();

    private GraphiteClient client;

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
            value = Double.parseDouble(((ConfigPattern) valueObject).applyValues(parserOutput));
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
    public void setUpModule(Map<String, Object> parameters)
    {
        String host = (String) parameters.get("host");
        Integer port = ((Number) parameters.get("port")).intValue();
        Long flushTimeMs = ((Number) parameters.get("flushTimeMs")).longValue();

        client = graphiteFactory.createClient(host, port, flushTimeMs);
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("host", String.class);
        config.addConfigValue("port", Number.class, false, 2003);
        config.addConfigValue("flushTimeMs", Number.class, false, 1000);
        return config;
    }

    @Override
    public String getModuleDescription()
    {
        return "sends metrics to Graphite";
    }
}
