package tv.ustream.yolo.module.processor;

import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.module.IModule;

import java.util.Map;

/**
 * @author bandesz
 */
public interface IProcessor extends IModule
{

    ConfigMap getProcessParamsConfig();

    void process(final Map<String, String> parserOutput, final Map<String, Object> processParams);

    void stop();

}
