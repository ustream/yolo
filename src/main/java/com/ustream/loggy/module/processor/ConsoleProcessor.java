package com.ustream.loggy.module.processor;

import com.ustream.loggy.config.ConfigException;

import java.util.List;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ConsoleProcessor implements IProcessor
{

    @Override
    public void validateProcessorParams(List<String> parserParams, Map<String, Object> params) throws ConfigException
    {
    }

    @Override
    public void process(Map<String, String> parserParams, Map<String, Object> processorParams)
    {
        System.out.format("Parser parameters: %s, processor parameters: %s\n", parserParams, processorParams);
    }

    @Override
    public void setUp(Map<String, Object> parameters, boolean debug)
    {
    }

}
