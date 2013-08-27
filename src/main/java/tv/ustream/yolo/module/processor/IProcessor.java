package tv.ustream.yolo.module.processor;

import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigGroup;
import tv.ustream.yolo.module.IModule;

import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public interface IProcessor extends IModule
{

    public ConfigGroup getProcessParamsConfig();

    public void validateProcessParams(List<String> parserOutputKeys, Map<String, Object> params) throws ConfigException;

    public void process(Map<String, String> parserOutput, Map<String, Object> processParams);

}
