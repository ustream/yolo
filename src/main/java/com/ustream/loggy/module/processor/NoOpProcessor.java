package com.ustream.loggy.module.processor;

import com.ustream.loggy.config.ConfigException;
import com.ustream.loggy.config.ConfigGroup;

import java.util.List;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class NoOpProcessor implements IProcessor
{

    @Override
    public void validateProcessorParams(List<String> parserParams, Map<String, Object> params) throws ConfigException
    {
    }

    @Override
    public void process(Map<String, String> parserParams, Map<String, Object> processorParams)
    {
    }

    @Override
    public ConfigGroup getProcessorParamsConfig()
    {
        return null;
    }

    @Override
    public void setUpModule(Map<String, Object> parameters, boolean debug)
    {
    }

    @Override
    public ConfigGroup getModuleConfig()
    {
        return null;
    }

    @Override
    public String getModuleDescription()
    {
        return "does nothing, use it if you want to disable a parser temporarily";
    }

}
