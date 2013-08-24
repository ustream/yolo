package com.ustream.loggy.module.processor;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ConsoleProcessorTest
{

    private final PrintStream oldSystemOut = System.out;

    private final ByteArrayOutputStream systemOut = new ByteArrayOutputStream();

    @Before
    public void setUp()
    {
        System.setOut(new PrintStream(systemOut));
    }

    @After
    public void tearDown()
    {
        System.setOut(oldSystemOut);
    }

    @Test
    public void shouldEchoResultToConsole()
    {
        ConsoleProcessor processor = new ConsoleProcessor();
        processor.setUp(new HashMap<String, Object>(), false);

        Map<String, String> parserParams = new HashMap<String, String>();
        parserParams.put("key1", "value1");

        Map<String, Object> processorParams = new HashMap<String, Object>();
        processorParams.put("key2", "value2");

        processor.process(parserParams, processorParams);

        String expected = "Parser parameters: {key1=value1}, processor parameters: {key2=value2}\n";

        Assert.assertEquals(expected, systemOut.toString());
    }

}
