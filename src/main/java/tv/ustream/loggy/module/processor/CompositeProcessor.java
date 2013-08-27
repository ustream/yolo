package tv.ustream.loggy.module.processor;

import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.config.ConfigGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class CompositeProcessor implements ICompositeProcessor
{

    private final List<IProcessor> processors = new ArrayList<IProcessor>();

    @Override
    public void validateProcessParams(List<String> parserOutputKeys, Map<String, Object> params) throws ConfigException
    {
        for (IProcessor processor : processors)
        {
            processor.validateProcessParams(parserOutputKeys, params);
        }
    }

    @Override
    public void process(Map<String, String> parserOutput, Map<String, Object> processParams)
    {
        for (IProcessor processor : processors)
        {
            processor.process(parserOutput, processParams);
        }
    }

    @Override
    public ConfigGroup getProcessParamsConfig()
    {
        ConfigGroup config = new ConfigGroup();
        for (IProcessor processor : processors)
        {
            config.merge(processor.getProcessParamsConfig());
        }
        return config;
    }

    @Override
    public void setUpModule(Map<String, Object> parameters)
    {
    }

    @Override
    public ConfigGroup getModuleConfig()
    {
        ConfigGroup config = new ConfigGroup();
        config.addConfigValue("processors", List.class);
        return config;
    }

    @Override
    public String getModuleDescription()
    {
        return "runs multiple processors";
    }

    @Override
    public void addProcessor(IProcessor processor)
    {
        processors.add(processor);
    }

}
