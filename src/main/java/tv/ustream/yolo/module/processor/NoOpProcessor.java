package tv.ustream.yolo.module.processor;

import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigMap;

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
    public ConfigMap getProcessParamsConfig()
    {
        return null;
    }

    @Override
    public void setUpModule(Map<String, Object> parameters)
    {
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        return null;
    }

    @Override
    public String getModuleDescription()
    {
        return "does nothing, use it if you want to disable a parser temporarily";
    }

}
