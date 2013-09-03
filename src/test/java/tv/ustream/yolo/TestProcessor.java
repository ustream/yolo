package tv.ustream.yolo;

import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.module.processor.IProcessor;

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
    public ConfigMap getProcessParamsConfig()
    {
        return null;
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
    public ConfigMap getModuleConfig()
    {
        return null;
    }

    @Override
    public String getModuleDescription()
    {
        return null;
    }
}
