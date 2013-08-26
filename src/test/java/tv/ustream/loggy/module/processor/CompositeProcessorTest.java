package tv.ustream.loggy.module.processor;

import org.junit.Before;
import org.junit.Test;
import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.module.ModuleFactory;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author bandesz
 */
public class CompositeProcessorTest
{

    private IProcessor p1;

    private IProcessor p2;

    @Before
    public void setUp()
    {
        p1 = mock(IProcessor.class);
        p2 = mock(IProcessor.class);
    }

    @Test
    public void processShouldClassSubProcessors() throws Exception
    {
        Map<String, String> parserOutput = new HashMap<String, String>();
        parserOutput.put("key1", "value1");

        Map<String, Object> processParams = new HashMap<String, Object>();
        processParams.put("key2", "value2");

        CompositeProcessor processor = createProcessor();
        processor.addProcessor(p1);
        processor.addProcessor(p2);

        processor.process(parserOutput, processParams);

        verify(p1).process(parserOutput, processParams);
        verify(p2).process(parserOutput, processParams);
    }

    @Test
    public void validateShouldClassSubProcessors() throws Exception
    {
        List<String> parserOutputKeys = new ArrayList<String>();
        parserOutputKeys.add("key1");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("key2", "value2");

        CompositeProcessor processor = createProcessor();
        processor.addProcessor(p1);
        processor.addProcessor(p2);

        processor.validateProcessParams(parserOutputKeys, params);

        verify(p1).validateProcessParams(parserOutputKeys, params);
        verify(p2).validateProcessParams(parserOutputKeys, params);
    }

    private CompositeProcessor createProcessor() throws ConfigException
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", CompositeProcessor.class.getCanonicalName());
        config.put("processors", Arrays.asList("x"));

        return (CompositeProcessor) new ModuleFactory().createProcessor("x", config, false);
    }
}
