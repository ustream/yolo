package tv.ustream.loggy.module;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.module.parser.IParser;
import tv.ustream.loggy.module.parser.PassThruParser;
import tv.ustream.loggy.module.processor.IProcessor;
import tv.ustream.loggy.module.processor.NoOpProcessor;

import java.util.HashMap;
import java.util.Map;

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
        config.put("class", NoOpProcessor.class.getCanonicalName());

        IProcessor processor = new ModuleFactory().createProcessor("x", config, false);

        Assert.assertEquals(NoOpProcessor.class, processor.getClass());
    }

    @Test
    public void createProcessorShouldThrowExceptionWhenConfigIsInvalid() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        Map<String, Object> config = new HashMap<String, Object>();

        IProcessor processor = new ModuleFactory().createProcessor("x", config, false);

        Assert.assertEquals(NoOpProcessor.class, processor.getClass());
    }

    @Test
    public void createParserShouldCreateNewInstance() throws ConfigException
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", PassThruParser.class.getCanonicalName());
        config.put("processor", "processor1");

        IParser parser = new ModuleFactory().createParser("x", config, false);

        Assert.assertEquals(PassThruParser.class, parser.getClass());
    }

    @Test
    public void createParserShouldThrowExceptionWhenConfigIsInvalid() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        Map<String, Object> config = new HashMap<String, Object>();

        IParser parser = new ModuleFactory().createParser("pr1", config, false);

        Assert.assertEquals(NoOpProcessor.class, parser.getClass());
    }

}
