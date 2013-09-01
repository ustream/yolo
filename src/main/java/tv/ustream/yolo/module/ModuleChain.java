package tv.ustream.yolo.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.config.ConfigPattern;
import tv.ustream.yolo.handler.ILineHandler;
import tv.ustream.yolo.module.parser.IParser;
import tv.ustream.yolo.module.processor.ICompositeProcessor;
import tv.ustream.yolo.module.processor.IProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class ModuleChain implements ILineHandler
{

    private static final Logger logger = LoggerFactory.getLogger(ModuleChain.class);

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

        if (processor == null)
        {
            return;
        }

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

        if (parser == null)
        {
            return;
        }

        parsers.put(name, parser);

        String processorName = (String) config.get("processor");

        if (!processors.containsKey(processorName))
        {
            throw new ConfigException(processorName + " processor does not exist");
        }

        Map<String, Object> params = (Map<String, Object>) config.get("processParams");

        if (params != null)
        {
            setProcessorParams(name, processorName, params);
        }

        transitions.put(name, processorName);
    }

    @SuppressWarnings("unchecked")
    private void setProcessorParams(String parserName, String processorName, Map<String, Object> params) throws ConfigException
    {
        ConfigMap processParamsConfig = processors.get(processorName).getProcessParamsConfig();
        if (processParamsConfig != null)
        {
            processParamsConfig.parse(parserName + ".processParams", params);
        }

        processParams.put(
            parserName,
            (Map<String, Object>) ConfigPattern.replacePatterns(params, parsers.get(parserName).getOutputKeys())
        );

        processors.get(processorName).validateProcessParams(parsers.get(parserName).getOutputKeys(), params);
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
