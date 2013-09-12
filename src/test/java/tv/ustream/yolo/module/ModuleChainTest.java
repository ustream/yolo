package tv.ustream.yolo.module;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.module.parser.IParser;
import tv.ustream.yolo.module.processor.CompositeProcessor;
import tv.ustream.yolo.module.processor.IProcessor;
import tv.ustream.yolo.module.reader.IReader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author bandesz
 */
@SuppressWarnings("unchecked")
public class ModuleChainTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private IReader reader1;

    private IParser parser1;

    private IParser parser2;

    private IProcessor processor1;

    private IProcessor processor2;

    private ModuleChain moduleChain;

    @Before
    public void setUp() throws Exception
    {
        ModuleFactory moduleFactory = mock(ModuleFactory.class);
        reader1 = mock(IReader.class);
        parser1 = mock(IParser.class);
        parser2 = mock(IParser.class);
        processor1 = mock(IProcessor.class);
        processor2 = mock(IProcessor.class);

        Map<String, Object> pr3Config = new HashMap<String, Object>();
        pr3Config.put("class", CompositeProcessor.class.getCanonicalName());
        pr3Config.put("processors", Arrays.asList("pr1", "pr2"));

        IProcessor processor3 = new ModuleFactory().createProcessor("pr3", pr3Config);

        when(moduleFactory.createReader(eq("r1"), anyMap())).thenReturn(reader1);
        when(moduleFactory.createParser(eq("pa1"), anyMap())).thenReturn(parser1);
        when(moduleFactory.createParser(eq("pa2"), anyMap())).thenReturn(parser2);
        when(moduleFactory.createParser(eq("pa3"), anyMap())).thenReturn(null);
        when(moduleFactory.createProcessor(eq("pr1"), anyMap())).thenReturn(processor1);
        when(moduleFactory.createProcessor(eq("pr2"), anyMap())).thenReturn(processor2);
        when(moduleFactory.createProcessor(eq("pr3"), anyMap())).thenReturn(processor3);

        moduleChain = new ModuleChain(moduleFactory);
    }

    @Test
    public void parserAndProcessorShouldBeAdded() throws Exception
    {
        Map<String, Object> config = new HashMap<String, Object>();

        addModule(config, "readers", "r1", createReaderConfig("reader1"));
        addModule(config, "processors", "pr1", createProcessorConfig("processor1"));
        addModule(config, "parsers", "pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));

        moduleChain.updateConfig(config, true);

        when(parser1.parse(anyString())).thenReturn(new HashMap<String, Object>());

        moduleChain.handle("some text");

        verify(processor1).process(anyMap(), anyMap());
    }

    @Test
    public void disabledParserShouldNotBeAdded() throws Exception
    {
        Map<String, Object> config = new HashMap<String, Object>();

        addModule(config, "readers", "r1", createReaderConfig("reader1"));
        addModule(config, "processors", "pr1", createProcessorConfig("processor1"));
        addModule(config, "parsers", "pa3", createParserConfig("parser3", "pr3", new HashMap<String, Object>()));

        moduleChain.updateConfig(config, true);

        when(parser1.parse(anyString())).thenReturn(new HashMap<String, Object>());

        moduleChain.handle("some text");

        verify(processor1, never()).process(anyMap(), anyMap());
    }

    @Test
    public void lineShouldBeParsedWithTheFirstApplicableParserProcessor() throws Exception
    {
        Map<String, Object> config = new HashMap<String, Object>();

        addModule(config, "readers", "r1", createReaderConfig("reader1"));
        addModule(config, "processors", "pr1", createProcessorConfig("processor1"));
        addModule(config, "processors", "pr2", createProcessorConfig("processor2"));
        addModule(config, "parsers", "pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));
        addModule(config, "parsers", "pa2", createParserConfig("parser2", "pr2", new HashMap<String, Object>()));

        moduleChain.updateConfig(config, true);

        when(parser1.parse(anyString())).thenReturn(null);
        when(parser2.parse(anyString())).thenReturn(new HashMap<String, Object>());

        moduleChain.handle("some text");

        verify(processor1, never()).process(anyMap(), anyMap());

        verify(processor2).process(anyMap(), anyMap());
    }

    @Test
    public void multipleHandleShouldWorkCorrectly() throws Exception
    {
        Map<String, Object> config = new HashMap<String, Object>();

        addModule(config, "readers", "r1", createReaderConfig("reader1"));
        addModule(config, "processors", "pr1", createProcessorConfig("processor1"));
        addModule(config, "processors", "pr2", createProcessorConfig("processor2"));
        addModule(config, "parsers", "pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));
        addModule(config, "parsers", "pa2", createParserConfig("parser2", "pr2", new HashMap<String, Object>()));

        moduleChain.updateConfig(config, true);

        when(parser1.parse("t1")).thenReturn(null);
        when(parser2.parse("t1")).thenReturn(new HashMap<String, Object>());
        when(parser1.parse("t2")).thenReturn(new HashMap<String, Object>());

        moduleChain.handle("t1");
        moduleChain.handle("t2");

        verify(processor1, times(1)).process(anyMap(), anyMap());

        verify(processor2, times(1)).process(anyMap(), anyMap());
    }

    @Test
    public void runAlwaysParsersShouldRunAlways() throws Exception
    {
        when(parser2.runAlways()).thenReturn(true);

        Map<String, Object> config = new HashMap<String, Object>();

        addModule(config, "readers", "r1", createReaderConfig("reader1"));
        addModule(config, "processors", "pr1", createProcessorConfig("processor1"));
        addModule(config, "processors", "pr2", createProcessorConfig("processor2"));
        addModule(config, "parsers", "pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));
        addModule(config, "parsers", "pa2", createParserConfig("parser2", "pr2", new HashMap<String, Object>()));

        moduleChain.updateConfig(config, true);

        when(parser1.parse(anyString())).thenReturn(new HashMap<String, Object>());
        when(parser2.parse(anyString())).thenReturn(new HashMap<String, Object>());

        moduleChain.handle("some text");

        verify(processor1).process(anyMap(), anyMap());

        verify(processor2).process(anyMap(), anyMap());
    }

    @Test
    public void processorShouldGetCorrectParameters() throws Exception
    {
        Map<String, Object> processParams = new HashMap<String, Object>();
        processParams.put("key1", "value1");

        Map<String, Object> config = new HashMap<String, Object>();

        addModule(config, "readers", "r1", createReaderConfig("reader1"));
        addModule(config, "processors", "pr1", createProcessorConfig("processor1"));
        addModule(config, "parsers", "pa1", createParserConfig("parser1", "pr1", processParams));

        moduleChain.updateConfig(config, true);

        Map<String, Object> parserOut = new HashMap<String, Object>();
        parserOut.put("key2", "value2");

        when(parser1.parse(anyString())).thenReturn(parserOut);

        moduleChain.handle("some text");

        verify(processor1).process(parserOut, processParams);
    }

    @Test
    public void nonExistingProcessorNameShouldThrowException() throws Exception
    {
        thrown.expect(ConfigException.class);

        Map<String, Object> config = new HashMap<String, Object>();

        addModule(config, "readers", "r1", createReaderConfig("reader1"));
        addModule(config, "processors", "pr1", createProcessorConfig("processor1"));
        addModule(config, "parsers", "pa1", createParserConfig("parser1", "prX", new HashMap<String, Object>()));

        moduleChain.updateConfig(config, true);
    }

    @Test
    public void compositeProcessorShouldCallItsSubProcessors() throws Exception
    {
        Map<String, Object> pr3Config = new HashMap<String, Object>();
        pr3Config.put("processors", Arrays.asList("pr1", "pr2"));

        Map<String, Object> config = new HashMap<String, Object>();

        addModule(config, "readers", "r1", createReaderConfig("reader1"));
        addModule(config, "processors", "pr1", createProcessorConfig("processor1"));
        addModule(config, "processors", "pr2", createProcessorConfig("processor2"));
        addModule(config, "processors", "pr3", pr3Config);
        addModule(config, "parsers", "pa1", createParserConfig("parser1", "pr3", new HashMap<String, Object>()));

        moduleChain.updateConfig(config, true);

        when(parser1.parse(anyString())).thenReturn(new HashMap<String, Object>());

        moduleChain.handle("some text");

        verify(processor1).process(anyMap(), anyMap());

        verify(processor2).process(anyMap(), anyMap());
    }

    @Test
    public void multipleProcessorsShouldBeCalled() throws Exception
    {
        Map<String, Object> pr3Config = new HashMap<String, Object>();
        pr3Config.put("processors", Arrays.asList("pr1", "pr2"));

        Map<String, Object> processors = new HashMap<String, Object>();
        processors.put("pr1", new HashMap<String, Object>());
        processors.put("pr2", new HashMap<String, Object>());

        Map<String, Object> parserConfig = new HashMap<String, Object>();
        parserConfig.put("class", "parser1");
        parserConfig.put("processors", processors);

        Map<String, Object> config = new HashMap<String, Object>();

        addModule(config, "readers", "r1", createReaderConfig("reader1"));
        addModule(config, "processors", "pr1", createProcessorConfig("processor1"));
        addModule(config, "processors", "pr2", createProcessorConfig("processor2"));
        addModule(config, "parsers", "pa1", parserConfig);

        moduleChain.updateConfig(config, true);

        when(parser1.parse(anyString())).thenReturn(new HashMap<String, Object>());

        moduleChain.handle("some text");

        verify(processor1).process(anyMap(), anyMap());

        verify(processor2).process(anyMap(), anyMap());
    }

    @Test
    public void configShouldBeUpdated() throws Exception
    {
        Map<String, Object> config = new HashMap<String, Object>();

        addModule(config, "readers", "r1", createReaderConfig("reader1"));
        addModule(config, "processors", "pr1", createProcessorConfig("processor1"));
        addModule(config, "parsers", "pa1", createParserConfig("parser1", "pr1", new HashMap<String, Object>()));

        moduleChain.updateConfig(config, true);

        when(parser1.parse("some text")).thenReturn(new HashMap<String, Object>());

        moduleChain.handle("some text");

        verify(processor1, times(1)).process(anyMap(), anyMap());

        Map<String, Object> config2 = new HashMap<String, Object>();

        addModule(config2, "readers", "r1", createReaderConfig("reader1"));
        addModule(config2, "processors", "pr2", createProcessorConfig("processor2"));
        addModule(config2, "parsers", "pa2", createParserConfig("parser2", "pr2", new HashMap<String, Object>()));

        moduleChain.updateConfig(config2, true);

        verify(processor1).stop();

        when(parser2.parse("some text")).thenReturn(new HashMap<String, Object>());

        moduleChain.handle("some text");

        verify(processor2, times(1)).process(anyMap(), anyMap());
    }

    private Map<String, Object> createReaderConfig(final String clazz)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", clazz);
        return config;
    }

    private Map<String, Object> createProcessorConfig(final String clazz)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", clazz);
        return config;
    }

    private Map<String, Object> createParserConfig(final String clazz, final String processor,
                                                   final Map<String, Object> processParams)
    {
        Map<String, Object> processors = new HashMap<String, Object>();
        processors.put(processor, processParams);

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", clazz);
        config.put("processors", processors);
        return config;
    }

    private void addModule(final Map config, final String type, final String name,
                           final Map<String, Object> moduleConfig)
    {
        if (!config.containsKey(type))
        {
            config.put(type, new HashMap<String, Object>());
        }

        ((Map<String, Object>) config.get(type)).put(name, moduleConfig);
    }

}
