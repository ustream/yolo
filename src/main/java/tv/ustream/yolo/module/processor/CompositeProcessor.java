package tv.ustream.yolo.module.processor;

import tv.ustream.yolo.config.ConfigMap;

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
    public void process(Map<String, String> parserOutput, Map<String, Object> processParams)
    {
        for (IProcessor processor : processors)
        {
            processor.process(parserOutput, processParams);
        }
    }

    @Override
    public ConfigMap getProcessParamsConfig()
    {
        ConfigMap config = new ConfigMap();
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
    public ConfigMap getModuleConfig()
    {
        ConfigMap config = new ConfigMap();
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
