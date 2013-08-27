package tv.ustream.loggy.module;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.module.parser.IParser;
import tv.ustream.loggy.module.processor.CompositeProcessor;
import tv.ustream.loggy.module.processor.IProcessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author bandesz
 */
@SuppressWarnings("unchecked")
public class ModuleChainTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private IParser parser1;

    private IParser parser2;

    private IProcessor processor1;

    private IProcessor processor2;

    private ModuleChain moduleChain;

    @Before
    public void setUp() throws Exception
    {
        ModuleFactory moduleFactory = mock(ModuleFactory.class);
        parser1 = mock(IParser.class);
        parser2 = mock(IParser.class);
        processor1 = mock(IProcessor.class);
        processor2 = mock(IProcessor.class);

        Map<String, Object> pr3Config = new HashMap<String, Object>();
        pr3Config.put("class", CompositeProcessor.class.getCanonicalName());
        pr3Config.put("processors", Arrays.asList("pr1", "pr2"));

        IProcessor processor3 = new ModuleFactory().createProcessor("pr3", pr3Config);

        when(moduleFactory.createParser(eq("pa1"), anyMap())).thenReturn(parser1);
        when(moduleFactory.createParser(eq("pa2"), anyMap())).thenReturn(parser2);
        when(moduleFactory.createProcessor(eq("pr1"), anyMap())).thenReturn(processor1);
        when(moduleFactory.createProcessor(eq("pr2"), anyMap())).thenReturn(processor2);
        when(moduleFactory.createProcessor(eq("pr3"), anyMap())).thenReturn(processor3);

        moduleChain = new ModuleChain(moduleFactory);
    }

    @Test
    public void parserAndProcessorShouldBeAdded() throws Exception
    {
        moduleChain.addProcessor("pr1", createProcessorConfig("processor1"));
        moduleChain.addParser("pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));

        when(parser1.parse(anyString())).thenReturn(new HashMap<String, String>());

        moduleChain.handle("some text");

        verify(processor1).process(anyMap(), anyMap());
    }

    @Test
    public void lineShouldBeParsedWithTheFirstApplicableParserProcessor() throws Exception
    {
        moduleChain.addProcessor("pr1", createProcessorConfig("processor1"));
        moduleChain.addProcessor("pr2", createProcessorConfig("processor2"));
        moduleChain.addParser("pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));
        moduleChain.addParser("pa2", createParserConfig("parser2", "pr2", new HashMap<String, Object>()));

        when(parser1.parse(anyString())).thenReturn(null);
        when(parser2.parse(anyString())).thenReturn(new HashMap<String, String>());

        moduleChain.handle("some text");

        verify(processor1, never()).process(anyMap(), anyMap());

        verify(processor2).process(anyMap(), anyMap());
    }

    @Test
    public void multipleHandleShouldWorkCorrectly() throws Exception
    {
        moduleChain.addProcessor("pr1", createProcessorConfig("processor1"));
        moduleChain.addProcessor("pr2", createProcessorConfig("processor2"));
        moduleChain.addParser("pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));
        moduleChain.addParser("pa2", createParserConfig("parser2", "pr2", new HashMap<String, Object>()));

        when(parser1.parse("t1")).thenReturn(null);
        when(parser2.parse("t1")).thenReturn(new HashMap<String, String>());
        when(parser1.parse("t2")).thenReturn(new HashMap<String, String>());

        moduleChain.handle("t1");
        moduleChain.handle("t2");

        verify(processor1, times(1)).process(anyMap(), anyMap());

        verify(processor2, times(1)).process(anyMap(), anyMap());
    }

    @Test
    public void runAlwaysParsersShouldRunAlways() throws Exception
    {
        when(parser2.runAlways()).thenReturn(true);

        moduleChain.addProcessor("pr1", createProcessorConfig("processor1"));
        moduleChain.addProcessor("pr2", createProcessorConfig("processor2"));
        moduleChain.addParser("pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));
        moduleChain.addParser("pa2", createParserConfig("parser2", "pr2", new HashMap<String, Object>()));

        when(parser1.parse(anyString())).thenReturn(new HashMap<String, String>());
        when(parser2.parse(anyString())).thenReturn(new HashMap<String, String>());

        moduleChain.handle("some text");

        verify(processor1).process(anyMap(), anyMap());

        verify(processor2).process(anyMap(), anyMap());
    }

    @Test
    public void processorShouldGetCorrectParameters() throws Exception
    {
        Map<String, Object> processParams = new HashMap<String, Object>();
        processParams.put("key1", "value1");

        moduleChain.addProcessor("pr1", createProcessorConfig("processor1"));
        moduleChain.addParser("pa1", createParserConfig("parser1", "pr1", processParams));

        Map<String, String> parserOut = new HashMap<String, String>();
        parserOut.put("key2", "value2");

        when(parser1.parse(anyString())).thenReturn(parserOut);

        moduleChain.handle("some text");

        verify(processor1).process(parserOut, processParams);
    }

    @Test
    public void nonExistingProcessorNameShouldThrowException() throws Exception
    {
        thrown.expect(ConfigException.class);

        moduleChain.addParser("pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));
    }

    @Test
    public void compositeProcessorShouldCallItsSubProcessors() throws Exception
    {
        Map<String, Object> pr3Config = new HashMap<String, Object>();
        pr3Config.put("processors", Arrays.asList("pr1", "pr2"));

        moduleChain.addProcessor("pr1", createProcessorConfig("processor1"));
        moduleChain.addProcessor("pr2", createProcessorConfig("processor2"));
        moduleChain.addProcessor("pr3", pr3Config);
        moduleChain.addParser("pa1", createParserConfig("parser1", "pr3", new HashMap<String, Object>()));

        when(parser1.parse(anyString())).thenReturn(new HashMap<String, String>());

        moduleChain.handle("some text");

        verify(processor1).process(anyMap(), anyMap());

        verify(processor2).process(anyMap(), anyMap());
    }

    private Map<String, Object> createProcessorConfig(String clazz)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", clazz);
        return config;
    }

    private Map<String, Object> createParserConfig(String clazz, String processor, Map<String, Object> processParams)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", clazz);
        config.put("processor", processor);
        config.put("processParams", processParams);
        return config;
    }

}
