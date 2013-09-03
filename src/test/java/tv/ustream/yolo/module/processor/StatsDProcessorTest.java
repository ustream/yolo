package tv.ustream.yolo.module.processor;

import com.timgroup.statsd.StatsDClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigPattern;
import tv.ustream.yolo.module.ModuleFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author bandesz
 */
public class StatsDProcessorTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private StatsDClient statsDClient;

    private StatsDProcessor processor;

    @Before
    public void setUp() throws ConfigException
    {
        statsDClient = mock(StatsDClient.class);

        processor = createProcessorMock("prefix", "host", 1234);
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void emptyHostShouldThrowException() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        processor = createProcessor("prefix1", null, 1234);
    }

    @Test
    public void processShouldSendStatsdCount()
    {
        Map<String, String> parserOutput = new HashMap<String, String>();

        processor.process(parserOutput, createprocessParams(StatsDProcessor.Types.COUNTER.value, "key", 5D));

        verify(statsDClient).count("key", 5);
    }

    @Test
    public void processShouldSendStatsdGauge()
    {
        Map<String, String> parserOutput = new HashMap<String, String>();

        processor.process(parserOutput, createprocessParams(StatsDProcessor.Types.GAUGE.value, "key", 5D));

        verify(statsDClient).gauge("key", 5);
    }

    @Test
    public void processShouldSendStatsdTime()
    {
        Map<String, String> parserOutput = new HashMap<String, String>();

        processor.process(parserOutput, createprocessParams(StatsDProcessor.Types.TIMER.value, "key", 5D));

        verify(statsDClient).time("key", 5);
    }

    @Test
    public void processShouldAddParamsToKey()
    {
        Map<String, String> parserOutput = new HashMap<String, String>();
        parserOutput.put("p1", "v1");

        ConfigPattern key = new ConfigPattern("some.#p1#.key");

        processor.process(parserOutput, createprocessParams(StatsDProcessor.Types.COUNTER.value, key, 5D));

        verify(statsDClient).count("some.v1.key", 5);
    }

    @Test
    public void processShouldUseValueFromParameters()
    {
        Map<String, String> parserOutput = new HashMap<String, String>();
        parserOutput.put("v1", "5");

        ConfigPattern value = new ConfigPattern("#v1#");

        processor.process(parserOutput, createprocessParams(StatsDProcessor.Types.COUNTER.value, "key", value));

        verify(statsDClient).count("key", 5);
    }

    @Test
    public void processShouldUseDynamicKeyAndValue()
    {
        Map<String, String> parserOutput = new HashMap<String, String>();
        parserOutput.put("p1", "v1");
        parserOutput.put("v1", "5");

        ConfigPattern key = new ConfigPattern("some.#p1#.key");
        ConfigPattern value = new ConfigPattern("#v1#");

        processor.process(parserOutput, createprocessParams(StatsDProcessor.Types.COUNTER.value, key, value));

        verify(statsDClient).count("some.v1.key", 5);
    }

    @Test
    public void processShouldSendMultipleKeys()
    {
        Map<String, Object> params = new HashMap<String, Object>();

        Map<String, Object> key1 = new HashMap<String, Object>();
        key1.put("type", "gauge");
        key1.put("key", new ConfigPattern("some.#p1#.key"));
        key1.put("value", 1D);
        key1.put("multiplier", 1D);

        Map<String, Object> key2 = new HashMap<String, Object>();
        key2.put("type", "timer");
        key2.put("key", new ConfigPattern("someother.#p1#.key"));
        key2.put("value", 2D);
        key2.put("multiplier", 1D);

        params.put("keys", Arrays.<Map>asList(key1, key2));

        Map<String, String> parserOutput = new HashMap<String, String>();
        parserOutput.put("p1", "v1");

        processor.process(parserOutput, params);

        verify(statsDClient).gauge("some.v1.key", 1);
        verify(statsDClient).time("someother.v1.key", 2);
    }

    @Test
    public void processShouldUseMultiplier()
    {
        Map<String, String> parserOutput = new HashMap<String, String>();

        processor.process(parserOutput, createprocessParams(StatsDProcessor.Types.COUNTER.value, "key", 5D, 10D));

        verify(statsDClient).count("key", 50);
    }

    private Map<String, Object> createprocessParams(String type, Object key, Object value)
    {
        return createprocessParams(type, key, value, 1D);
    }

    private Map<String, Object> createprocessParams(String type, Object key, Object value, Double multiplier)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, Object> key1 = new HashMap<String, Object>();
        key1.put("type", type);
        key1.put("key", key);
        key1.put("value", value);
        key1.put("multiplier", multiplier);
        params.put("keys", Arrays.<Map>asList(key1));
        return params;
    }

    private StatsDProcessor createProcessor(String prefix, String host, Integer port) throws ConfigException
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", StatsDProcessor.class.getCanonicalName());
        config.put("prefix", prefix);
        config.put("host", host);
        config.put("port", port.doubleValue());
        config.put("processor", "x");

        return (StatsDProcessor) new ModuleFactory().createProcessor("x", config);
    }

    private StatsDProcessor createProcessorMock(String prefix, String host, Integer port) throws ConfigException
    {
        StatsDProcessor processor = new StatsDProcessor()
        {
            protected StatsDClient createClient(String prefix, String host, int port)
            {
                return statsDClient;
            }
        };

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", StatsDProcessor.class.getCanonicalName());
        config.put("prefix", prefix);
        config.put("host", host);
        config.put("port", port.doubleValue());

        processor.setUpModule(config);

        return processor;
    }

}
