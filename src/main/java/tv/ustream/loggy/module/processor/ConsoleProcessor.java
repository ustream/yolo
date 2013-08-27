package tv.ustream.loggy.module.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.config.ConfigGroup;

import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class ConsoleProcessor implements IProcessor
{

    private Logger logger = LoggerFactory.getLogger(ConsoleProcessor.class);

    @Override
    public void validateProcessParams(List<String> parserOutputKeys, Map<String, Object> params) throws ConfigException
    {
    }

    @Override
    public void process(Map<String, String> parserOutput, Map<String, Object> processParams)
    {
        logger.info("Parser output: {}, process parameters: {}", parserOutput, processParams);
    }

    @Override
    public ConfigGroup getProcessParamsConfig()
    {
        return null;
    }

    @Override
    public void setUpModule(Map<String, Object> parameters, boolean debug)
    {
    }

    @Override
    public ConfigGroup getModuleConfig()
    {
        return null;
    }

    @Override
    public String getModuleDescription()
    {
        return "writes parameters to console, use it for debug purposes";
    }

}
