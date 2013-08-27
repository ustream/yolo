package tv.ustream.loggy;

import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.config.ConfigGroup;
import tv.ustream.loggy.module.processor.IProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class TestProcessor implements IProcessor
{

    public static List<String> data = new ArrayList<String>();

    @Override
    public ConfigGroup getProcessParamsConfig()
    {
        return null;
    }

    @Override
    public void validateProcessParams(List<String> parserOutputKeys, Map<String, Object> params) throws ConfigException
    {
    }

    @Override
    public void process(Map<String, String> parserOutput, Map<String, Object> processParams)
    {
        data.add(String.valueOf(parserOutput) + "|" + String.valueOf(processParams));
    }

    @Override
    public void setUpModule(Map<String, Object> parameters)
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
        return null;
    }
}
