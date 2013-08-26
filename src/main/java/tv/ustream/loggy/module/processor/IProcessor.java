package tv.ustream.loggy.module.processor;

import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.config.ConfigGroup;
import tv.ustream.loggy.module.IModule;

import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public interface IProcessor extends IModule
{

    public ConfigGroup getProcessorParamsConfig();

    public void validateProcessorParams(List<String> parserParams, Map<String, Object> params) throws ConfigException;

    public void process(Map<String, String> parserParams, Map<String, Object> processorParams);

}
