package tv.ustream.yolo.module;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.module.parser.IParser;
import tv.ustream.yolo.module.parser.PassThruParser;
import tv.ustream.yolo.module.processor.ConsoleProcessor;
import tv.ustream.yolo.module.processor.IProcessor;

/**
 * @author bandesz
 */
public class ModuleFactoryTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createProcessorShouldCreateNewInstance() throws ConfigException
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", ConsoleProcessor.class.getCanonicalName());

        IProcessor processor = new ModuleFactory().createProcessor("x", config);

        Assert.assertEquals(ConsoleProcessor.class, processor.getClass());
    }

    @Test
    public void createProcessorShouldThrowExceptionWhenConfigIsInvalid() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        Map<String, Object> config = new HashMap<String, Object>();

        IProcessor processor = new ModuleFactory().createProcessor("x", config);

        Assert.assertEquals(ConsoleProcessor.class, processor.getClass());
    }

    @Test
    public void createParserShouldCreateNewInstance() throws ConfigException
    {
        Map<String, Object> processors = new HashMap<String, Object>();
        processors.put("processor1", new HashMap<String, Object>());

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", PassThruParser.class.getCanonicalName());
        config.put("processors", processors);

        IParser parser = new ModuleFactory().createParser("x", config);

        Assert.assertEquals(PassThruParser.class, parser.getClass());
    }

    @Test
    public void createParserShouldThrowExceptionWhenConfigIsInvalid() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        Map<String, Object> config = new HashMap<String, Object>();

        IParser parser = new ModuleFactory().createParser("pr1", config);

        Assert.assertEquals(ConsoleProcessor.class, parser.getClass());
    }

    @Test
    public void createParserShouldReturnNullWhenNotEnabled() throws ConfigException
    {
        Map<String, Object> processors = new HashMap<String, Object>();
        processors.put("processor1", new HashMap<String, Object>());

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", PassThruParser.class.getCanonicalName());
        config.put("processors", processors);
        config.put("enabled", false);

        IParser parser = new ModuleFactory().createParser("x", config);

        Assert.assertNull(parser);
    }

}
