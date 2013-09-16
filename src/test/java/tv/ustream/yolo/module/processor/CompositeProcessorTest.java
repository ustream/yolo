package tv.ustream.yolo.module.processor;

import org.junit.Before;
import org.junit.Test;
import tv.ustream.yolo.module.ModuleFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    private CompositeProcessor createProcessor() throws Exception
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", CompositeProcessor.class.getCanonicalName());
        config.put("processors", Arrays.asList("x"));

        return (CompositeProcessor) new ModuleFactory().createProcessor("x", config);
    }
}
