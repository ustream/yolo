package tv.ustream.loggy.handler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.module.ModuleFactory;
import tv.ustream.loggy.module.parser.IParser;
import tv.ustream.loggy.module.processor.IProcessor;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author bandesz
 */
@SuppressWarnings("unchecked")
public class DataHandlerTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private IParser parser1;

    private IParser parser2;

    private IProcessor processor1;

    private IProcessor processor2;

    private DataHandler dataHandler;

    @Before
    public void setUp() throws Exception
    {
        ModuleFactory moduleFactory = mock(ModuleFactory.class);
        parser1 = mock(IParser.class);
        parser2 = mock(IParser.class);
        processor1 = mock(IProcessor.class);
        processor2 = mock(IProcessor.class);

        when(moduleFactory.createParser(eq("pa1"), anyMap(), anyBoolean())).thenReturn(parser1);
        when(moduleFactory.createParser(eq("pa2"), anyMap(), anyBoolean())).thenReturn(parser2);
        when(moduleFactory.createProcessor(eq("pr1"), anyMap(), anyBoolean())).thenReturn(processor1);
        when(moduleFactory.createProcessor(eq("pr2"), anyMap(), anyBoolean())).thenReturn(processor2);

        dataHandler = new DataHandler(moduleFactory, false);
    }

    @Test
    public void lineShouldBeParsedWithTheFirstApplicableParserProcessor() throws Exception
    {
        dataHandler.addProcessor("pr1", createProcessorConfig("processor1"));
        dataHandler.addProcessor("pr2", createProcessorConfig("processor2"));
        dataHandler.addParser("pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));
        dataHandler.addParser("pa2", createParserConfig("parser2", "pr2", new HashMap<String, Object>()));

        when(parser1.parse(anyString())).thenReturn(null);
        when(parser2.parse(anyString())).thenReturn(new HashMap<String, String>());

        dataHandler.handle("some text");

        verify(processor1, never()).process(anyMap(), anyMap());

        verify(processor2).process(anyMap(), anyMap());
    }

    @Test
    public void multipleHandleShouldWorkCorrectly() throws Exception
    {
        dataHandler.addProcessor("pr1", createProcessorConfig("processor1"));
        dataHandler.addProcessor("pr2", createProcessorConfig("processor2"));
        dataHandler.addParser("pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));
        dataHandler.addParser("pa2", createParserConfig("parser2", "pr2", new HashMap<String, Object>()));

        when(parser1.parse("t1")).thenReturn(null);
        when(parser2.parse("t1")).thenReturn(new HashMap<String, String>());
        when(parser1.parse("t2")).thenReturn(new HashMap<String, String>());

        dataHandler.handle("t1");
        dataHandler.handle("t2");

        verify(processor1, times(1)).process(anyMap(), anyMap());

        verify(processor2, times(1)).process(anyMap(), anyMap());
    }

    @Test
    public void runAlwaysParsersShouldRunAlways() throws Exception
    {
        when(parser2.runAlways()).thenReturn(true);

        dataHandler.addProcessor("pr1", createProcessorConfig("processor1"));
        dataHandler.addProcessor("pr2", createProcessorConfig("processor2"));
        dataHandler.addParser("pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));
        dataHandler.addParser("pa2", createParserConfig("parser2", "pr2", new HashMap<String, Object>()));

        when(parser1.parse(anyString())).thenReturn(new HashMap<String, String>());
        when(parser2.parse(anyString())).thenReturn(new HashMap<String, String>());

        dataHandler.handle("some text");

        verify(processor1).process(anyMap(), anyMap());

        verify(processor2).process(anyMap(), anyMap());
    }

    @Test
    public void processorShouldGetCorrectParameters() throws Exception
    {
        Map<String, Object> processorParams = new HashMap<String, Object>();
        processorParams.put("key1", "value1");

        dataHandler.addProcessor("pr1", createProcessorConfig("processor1"));
        dataHandler.addParser("pa1", createParserConfig("parser1", "pr1", processorParams));

        Map<String, String> parserOut = new HashMap<String, String>();
        parserOut.put("key2", "value2");

        when(parser1.parse(anyString())).thenReturn(parserOut);

        dataHandler.handle("some text");

        verify(processor1).process(parserOut, processorParams);
    }

    @Test
    public void nonExistingProcessorNameShouldThrowException() throws Exception
    {
        thrown.expect(ConfigException.class);

        dataHandler.addParser("pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));
    }

    private Map<String, Object> createProcessorConfig(String clazz)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", clazz);
        return config;
    }

    private Map<String, Object> createParserConfig(String clazz, String processor, Map<String, Object> processorParams)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", clazz);
        config.put("processor", processor);
        config.put("processorParams", processorParams);
        return config;
    }

}
