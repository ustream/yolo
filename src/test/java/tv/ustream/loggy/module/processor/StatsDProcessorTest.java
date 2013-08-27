package tv.ustream.loggy.module.processor;

import com.timgroup.statsd.StatsDClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.config.ConfigPattern;
import tv.ustream.loggy.module.ModuleFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public void validateShouldThrowExceptionWhenValueNameIsMissing() throws Exception
    {
        thrown.expect(ConfigException.class);

        processor.validateProcessParams(new ArrayList<String>(), createprocessParams(StatsDProcessor.TYPE_COUNTER, "key", "value"));
    }

    @Test
    public void validateValidConfigPattern() throws Exception
    {
        ConfigPattern key = new ConfigPattern("some text #p1#");

        List<String> parserOutputKeys = new ArrayList<String>();
        parserOutputKeys.add("value");

        processor.validateProcessParams(parserOutputKeys, createprocessParams(StatsDProcessor.TYPE_COUNTER, key, "value"));
    }

    @Test
    public void processShouldSendStatsdCount() throws ConfigException
    {
        Map<String, String> parserOutput = new HashMap<String, String>();

        processor.process(parserOutput, createprocessParams(StatsDProcessor.TYPE_COUNTER, "key", 5D));

        verify(statsDClient).count("key", 5);
    }

    @Test
    public void processShouldSendStatsdGauge() throws ConfigException
    {
        Map<String, String> parserOutput = new HashMap<String, String>();

        processor.process(parserOutput, createprocessParams(StatsDProcessor.TYPE_GAUGE, "key", 5D));

        verify(statsDClient).gauge("key", 5);
    }

    @Test
    public void processShouldSendStatsdTime() throws ConfigException
    {
        Map<String, String> parserOutput = new HashMap<String, String>();

        processor.process(parserOutput, createprocessParams(StatsDProcessor.TYPE_TIMER, "key", 5D));

        verify(statsDClient).time("key", 5);
    }

    @Test
    public void processShouldAddParamsToKey()
    {
        Map<String, String> parserOutput = new HashMap<String, String>();
        parserOutput.put("p1", "v1");

        ConfigPattern key = new ConfigPattern("some.#p1#.key");

        processor.process(parserOutput, createprocessParams(StatsDProcessor.TYPE_COUNTER, key, 5D));

        verify(statsDClient).count("some.v1.key", 5);
    }

    @Test
    public void processShouldUseValueFromParameters()
    {
        Map<String, String> parserOutput = new HashMap<String, String>();
        parserOutput.put("v1", "5");

        processor.process(parserOutput, createprocessParams(StatsDProcessor.TYPE_COUNTER, "key", "v1"));

        verify(statsDClient).count("key", 5);
    }

    @Test
    public void processShouldUseDynamicKeyAndValue()
    {
        Map<String, String> parserOutput = new HashMap<String, String>();
        parserOutput.put("p1", "v1");
        parserOutput.put("v1", "5");

        ConfigPattern key = new ConfigPattern("some.#p1#.key");

        processor.process(parserOutput, createprocessParams(StatsDProcessor.TYPE_COUNTER, key, "v1"));

        verify(statsDClient).count("some.v1.key", 5);
    }

    private Map<String, Object> createprocessParams(String type, Object key, Object value)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("type", type);
        config.put("key", key);
        config.put("value", value);
        return config;
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
