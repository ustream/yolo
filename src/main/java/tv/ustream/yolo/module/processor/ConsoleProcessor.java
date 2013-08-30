package tv.ustream.yolo.module.processor;

import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigMap;

import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class ConsoleProcessor implements IProcessor
{

    @Override
    public void validateProcessParams(List<String> parserOutputKeys, Map<String, Object> params) throws ConfigException
    {
    }

    @Override
    public void process(Map<String, String> parserOutput, Map<String, Object> processParams)
    {
        System.out.format("[console] Parser output: %s, process parameters: %s%n", parserOutput, processParams);
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
        return "writes parameters to console, use it for debug purposes";
    }

}
