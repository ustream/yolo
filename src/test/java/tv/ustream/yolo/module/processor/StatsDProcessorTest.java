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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author bandesz
 */
public class StatsDProcessorTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private StatsDFactory statsDFactory;

    private StatsDClient statsDClient;

    private StatsDProcessor processor;

    @Before
    public void setUp() throws ConfigException
    {
        statsDFactory = mock(StatsDFactory.class);
        statsDClient = mock(StatsDClient.class);

        when(statsDFactory.createClient(anyString(), anyString(), anyInt())).thenReturn(statsDClient);

        StatsDProcessor.setStatsDFactory(statsDFactory);

        processor = createProcessor("prefix1", "host1", 1234);
    }

    @After
    public void tearDown()
    {
        StatsDProcessor.setStatsDFactory(new StatsDFactory());
    }

    @Test
    public void testSetup() throws ConfigException
    {
        verify(statsDFactory).createClient("prefix1", "host1", 1234);
    }

    @Test
    public void emptyHostShouldThrowException() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        createProcessor("prefix1", null, 1234);
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

        Map<String, Object> key2 = new HashMap<String, Object>();
        key2.put("type", "timer");
        key2.put("key", new ConfigPattern("someother.#p1#.key"));
        key2.put("value", 2D);

        params.put("keys", Arrays.<Map>asList(key1, key2));

        Map<String, String> parserOutput = new HashMap<String, String>();
        parserOutput.put("p1", "v1");

        processor.process(parserOutput, params);

        verify(statsDClient).gauge("some.v1.key", 1);
        verify(statsDClient).time("someother.v1.key", 2);
    }

    private Map<String, Object> createprocessParams(String type, Object key, Object value)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, Object> key1 = new HashMap<String, Object>();
        key1.put("type", type);
        key1.put("key", key);
        key1.put("value", value);
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

}
