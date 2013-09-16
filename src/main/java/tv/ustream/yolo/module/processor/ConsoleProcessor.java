package tv.ustream.yolo.module.processor;

import tv.ustream.yolo.config.ConfigMap;

import java.util.Map;

/**
 * @author bandesz
 */
public class ConsoleProcessor implements IProcessor
{

    @Override
    public void process(final Map<String, String> parserOutput, final Map<String, Object> processParams)
    {
        System.out.format("[console] Parser output: %s, process parameters: %s%n", parserOutput, processParams);
    }

    @Override
    public ConfigMap getProcessParamsConfig()
    {
        return null;
    }

    @Override
    public void setUpModule(final Map<String, Object> parameters)
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

    @Override
    public void stop()
    {
    }

}
