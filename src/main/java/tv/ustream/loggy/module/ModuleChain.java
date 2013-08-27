package tv.ustream.loggy.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.config.ConfigGroup;
import tv.ustream.loggy.config.ConfigPattern;
import tv.ustream.loggy.handler.ILineHandler;
import tv.ustream.loggy.module.parser.IParser;
import tv.ustream.loggy.module.processor.ICompositeProcessor;
import tv.ustream.loggy.module.processor.IProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class ModuleChain implements ILineHandler
{

    private final Logger logger = LoggerFactory.getLogger(ModuleChain.class);


    private final ModuleFactory moduleFactory;

    private final Map<String, IParser> parsers = new HashMap<String, IParser>();

    private final Map<String, Map<String, Object>> processParams = new HashMap<String, Map<String, Object>>();

    private final Map<String, IProcessor> processors = new HashMap<String, IProcessor>();

    private final Map<String, String> transitions = new HashMap<String, String>();

    public ModuleChain(ModuleFactory moduleFactory)
    {
        this.moduleFactory = moduleFactory;
    }

    @SuppressWarnings("unchecked")
    public void addProcessor(String name, Map<String, Object> config) throws Exception
    {
        logger.debug("Adding {} processor {}", name, config);

        IProcessor processor = moduleFactory.createProcessor(name, config);
        processors.put(name, processor);

        if (processor instanceof ICompositeProcessor)
        {
            setupCompositeProcessor((ICompositeProcessor) processor, (List<String>) config.get("processors"));
        }
    }

    private void setupCompositeProcessor(ICompositeProcessor processor, List<String> subProcessors) throws Exception
    {
        for (String subProcessor : subProcessors)
        {
            if (processors.containsKey(subProcessor))
            {
                processor.addProcessor(processors.get(subProcessor));
            }
            else
            {
                throw new ConfigException(subProcessor + " processor does not exist!");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void addParser(String name, Map<String, Object> config) throws Exception
    {
        logger.debug("Adding {} parser {}", name, config);

        IParser parser = moduleFactory.createParser(name, config);
        parsers.put(name, parser);

        String processorName = (String) config.get("processor");

        if (!processors.containsKey(processorName))
        {
            throw new ConfigException(processorName + " processor does not exist");
        }

        Map<String, Object> params = (Map<String, Object>) config.get("processParams");

        if (params != null)
        {
            ConfigGroup processParamsConfig = processors.get(processorName).getProcessParamsConfig();
            if (processParamsConfig != null)
            {
                processParamsConfig.parseValues(name + ".processParams", params);
            }

            processParams.put(name, ConfigPattern.replacePatterns(params));

            processors.get(processorName).validateProcessParams(parser.getOutputKeys(), params);
        }

        transitions.put(name, processorName);
    }

    public void handle(String line)
    {
        Boolean match = false;
        for (String parserName : parsers.keySet())
        {
            if (!match || parsers.get(parserName).runAlways())
            {
                Map<String, String> parserOutput = parsers.get(parserName).parse(line);
                if (parserOutput != null)
                {
                    match = true;
                    processors.get(transitions.get(parserName)).process(parserOutput, processParams.get(parserName));
                }
            }
        }
    }

}
