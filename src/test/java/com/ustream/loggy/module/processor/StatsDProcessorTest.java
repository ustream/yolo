package com.ustream.loggy.module.processor;

import com.timgroup.statsd.StatsDClient;
import com.ustream.loggy.config.ConfigException;
import com.ustream.loggy.config.ConfigPattern;
import com.ustream.loggy.module.ModuleFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author bandesz <bandesz@ustream.tv>
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
    public void validateShouldThrowExceptionForInvalidType() throws Exception
    {
        thrown.expect(ConfigException.class);

        processor.validateProcessorParams(new ArrayList<String>(), createProcessorParams("xxx", "key", 1));
    }

    @Test
    public void validateShouldThrowExceptionForInvalidKey() throws Exception
    {
        thrown.expect(ConfigException.class);

        processor.validateProcessorParams(new ArrayList<String>(), createProcessorParams("count", "", 1));
    }

    @Test
    public void validateShouldThrowExceptionForInvalidValue() throws Exception
    {
        thrown.expect(ConfigException.class);

        processor.validateProcessorParams(new ArrayList<String>(), createProcessorParams("count", "key", ""));
    }

    @Test
    public void validateShouldThrowExceptionWhenValueNameIsMissing() throws Exception
    {
        thrown.expect(ConfigException.class);

        processor.validateProcessorParams(new ArrayList<String>(), createProcessorParams("count", "key", "value"));
    }

    @Test
    public void validateValidConfigPattern() throws Exception
    {
        ConfigPattern key = new ConfigPattern("some text #p1#");

        List<String> parserParams = new ArrayList<String>();
        parserParams.add("value");

        processor.validateProcessorParams(parserParams, createProcessorParams("count", key, "value"));
    }

    @Test
    public void processShouldSendStatsdCount() throws ConfigException
    {
        Map<String, String> parserParams = new HashMap<String, String>();

        processor.process(parserParams, createProcessorParams("count", "key", 5D));

        verify(statsDClient).count("key", 5);
    }

    @Test
    public void processShouldSendStatsdGauge() throws ConfigException
    {
        Map<String, String> parserParams = new HashMap<String, String>();

        processor.process(parserParams, createProcessorParams("gauge", "key", 5D));

        verify(statsDClient).gauge("key", 5);
    }

    @Test
    public void processShouldSendStatsdTime() throws ConfigException
    {
        Map<String, String> parserParams = new HashMap<String, String>();

        processor.process(parserParams, createProcessorParams("time", "key", 5D));

        verify(statsDClient).time("key", 5);
    }

    @Test
    public void processShouldAddParamsToKey()
    {
        Map<String, String> parserParams = new HashMap<String, String>();
        parserParams.put("p1", "v1");

        ConfigPattern key = new ConfigPattern("some.#p1#.key");

        processor.process(parserParams, createProcessorParams("count", key, 5D));

        verify(statsDClient).count("some.v1.key", 5);
    }

    @Test
    public void processShouldUseValueFromParameters()
    {
        Map<String, String> parserParams = new HashMap<String, String>();
        parserParams.put("v1", "5");

        processor.process(parserParams, createProcessorParams("count", "key", "v1"));

        verify(statsDClient).count("key", 5);
    }

    @Test
    public void processShouldUseDynamicKeyAndValue()
    {
        Map<String, String> parserParams = new HashMap<String, String>();
        parserParams.put("p1", "v1");
        parserParams.put("v1", "5");

        ConfigPattern key = new ConfigPattern("some.#p1#.key");

        processor.process(parserParams, createProcessorParams("count", key, "v1"));

        verify(statsDClient).count("some.v1.key", 5);
    }

    private Map<String, Object> createProcessorParams(String type, Object key, Object value)
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

        StatsDProcessor processor = (StatsDProcessor) new ModuleFactory().createProcessor("x", config, false);
        return processor;
    }

}
