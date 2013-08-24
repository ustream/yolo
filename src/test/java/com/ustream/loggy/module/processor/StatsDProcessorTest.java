package com.ustream.loggy.module.processor;

import com.timgroup.statsd.StatsDClient;
import com.ustream.loggy.config.ConfigException;
import com.ustream.loggy.config.ConfigPattern;
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

    private StatsDFactory factory;

    private StatsDClient statsDClient;

    private StatsDProcessor processor;

    @Before
    public void setUp()
    {
        factory = mock(StatsDFactory.class);
        statsDClient = mock(StatsDClient.class);

        when(factory.createClient(anyString(), anyString(), anyInt())).thenReturn(statsDClient);

        processor = new StatsDProcessor();
        processor.setStatsDFactory(factory);
    }

    @Test
    public void testSetup()
    {
        processor.setUp(createConfig("prefix1", "host1", 1234), false);

        verify(factory).createClient("prefix1", "host1", 1234);
    }

    @Test
    public void emptyHostShouldThrowException() throws Exception
    {
        thrown.expect(IllegalArgumentException.class);

        processor.setUp(createConfig("prefix1", null, 1234), false);
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
    public void processShouldSendStatsdCount()
    {
        processor.setUp(createConfig("prefix", "host", 1234), false);

        Map<String, String> parserParams = new HashMap<String, String>();

        processor.process(parserParams, createProcessorParams("count", "key", 5D));

        verify(statsDClient).count("key", 5);
    }

    @Test
    public void processShouldSendStatsdGauge()
    {
        processor.setUp(createConfig("prefix", "host", 1234), false);

        Map<String, String> parserParams = new HashMap<String, String>();

        processor.process(parserParams, createProcessorParams("gauge", "key", 5D));

        verify(statsDClient).gauge("key", 5);
    }

    @Test
    public void processShouldSendStatsdTime()
    {
        processor.setUp(createConfig("prefix", "host", 1234), false);

        Map<String, String> parserParams = new HashMap<String, String>();

        processor.process(parserParams, createProcessorParams("time", "key", 5D));

        verify(statsDClient).time("key", 5);
    }

    @Test
    public void processShouldAddParamsToKey()
    {
        processor.setUp(createConfig("prefix", "host", 1234), false);

        Map<String, String> parserParams = new HashMap<String, String>();
        parserParams.put("p1", "v1");

        ConfigPattern key = new ConfigPattern("some.#p1#.key");

        processor.process(parserParams, createProcessorParams("count", key, 5D));

        verify(statsDClient).count("some.v1.key", 5);
    }

    @Test
    public void processShouldUseValueFromParameters()
    {
        processor.setUp(createConfig("prefix", "host", 1234), false);

        Map<String, String> parserParams = new HashMap<String, String>();
        parserParams.put("v1", "5");

        processor.process(parserParams, createProcessorParams("count", "key", "v1"));

        verify(statsDClient).count("key", 5);
    }

    @Test
    public void processShouldUseDynamicKeyAndValue()
    {
        processor.setUp(createConfig("prefix", "host", 1234), false);

        Map<String, String> parserParams = new HashMap<String, String>();
        parserParams.put("p1", "v1");
        parserParams.put("v1", "5");

        ConfigPattern key = new ConfigPattern("some.#p1#.key");

        processor.process(parserParams, createProcessorParams("count", key, "v1"));

        verify(statsDClient).count("some.v1.key", 5);
    }

    private Map<String, Object> createConfig(String prefix, String host, Integer port)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("prefix", prefix);
        config.put("host", host);
        config.put("port", port.doubleValue());
        return config;
    }

    private Map<String, Object> createProcessorParams(String type, Object key, Object value)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("type", type);
        config.put("key", key);
        config.put("value", value);
        return config;
    }

}
