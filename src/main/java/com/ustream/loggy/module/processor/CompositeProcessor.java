package com.ustream.loggy.module.processor;

import com.ustream.loggy.config.ConfigException;
import com.ustream.loggy.config.ConfigGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class CompositeProcessor implements IProcessor, ICompositeProcessor
{

    private final List<IProcessor> processors = new ArrayList<IProcessor>();

    @Override
    public void validateProcessorParams(List<String> parserParams, Map<String, Object> params) throws ConfigException
    {
        for (IProcessor processor : processors)
        {
            processor.validateProcessorParams(parserParams, params);
        }
    }

    @Override
    public void process(Map<String, String> parserParams, Map<String, Object> processorParams)
    {
        for (IProcessor processor : processors)
        {
            processor.process(parserParams, processorParams);
        }
    }

    @Override
    public ConfigGroup getProcessorParamsConfig()
    {
        ConfigGroup config = new ConfigGroup();
        for (IProcessor processor : processors)
        {
            config.merge(processor.getProcessorParamsConfig());
        }
        return config;
    }

    @Override
    public void setUpModule(Map<String, Object> parameters, boolean debug)
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
