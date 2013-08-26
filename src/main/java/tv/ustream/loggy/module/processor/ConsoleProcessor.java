package tv.ustream.loggy.module.processor;

import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.config.ConfigGroup;

import java.util.List;
import java.util.Map;

/**
 * @author bandesz
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
        return "writes parameters to console, use it for debug purposes";
    }

}
