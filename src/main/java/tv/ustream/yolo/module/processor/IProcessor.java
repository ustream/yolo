package tv.ustream.yolo.module.processor;

import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.module.IModule;

import java.util.Map;

/**
 * @author bandesz
 */
public interface IProcessor extends IModule
{

    public ConfigMap getProcessParamsConfig();

    public void process(Map<String, String> parserOutput, Map<String, Object> processParams);

}
