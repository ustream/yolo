package tv.ustream.loggy.module.processor;

import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.config.ConfigGroup;

import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class NoOpProcessor implements IProcessor
{

    @Override
    public void validateProcessParams(List<String> parserOutputKeys, Map<String, Object> params) throws ConfigException
    {
    }

    @Override
    public void process(Map<String, String> parserOutput, Map<String, Object> processParams)
    {
    }

    @Override
    public ConfigGroup getProcessParamsConfig()
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
