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

    private static List<String> data = new ArrayList<String>();

    public static List<String> getData()
    {
        return data;
    }

    public static void reset()
    {
        data = new ArrayList<String>();
    }

    @Override
    public ConfigMap getProcessParamsConfig()
    {
        return null;
    }

    @Override
    public void process(final Map<String, Object> parserOutput, final Map<String, Object> processParams)
    {
        getData().add(String.valueOf(parserOutput) + "|" + String.valueOf(processParams));
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
        return null;
    }

    @Override
    public void stop()
    {
    }

}
