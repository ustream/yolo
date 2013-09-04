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

    private Map<String, Object> config = null;

    public ModuleChain(ModuleFactory moduleFactory)
    {
        this.moduleFactory = moduleFactory;
    }

    private ConfigMap getMainConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("processors", Map.class);
        config.addConfigValue("parsers", Map.class);
        return config;
    }

    public void updateConfig(Map<String, Object> config, boolean instant) throws ConfigException
    {
        this.config = config;

        getMainConfig().parse("[root]", config);

        if (instant)
        {
            update();
        }
    }

    private void update() throws ConfigException
    {
        if (config == null)
        {
            return;
        }

        stop();

        reset();

        Map<String, Object> processorsEntry = (Map<String, Object>) config.get("processors");
        for (Map.Entry<String, Object> processor : processorsEntry.entrySet())
        {
            addProcessor(processor.getKey(), (Map<String, Object>) processor.getValue());
        }

        for (Map.Entry<String, Object> processor : processorsEntry.entrySet())
        {
            setupCompositeProcessor(processors.get(processor.getKey()), (Map<String, Object>) processor.getValue());
        }

        Map<String, Object> parsersEntry = (Map<String, Object>) config.get("parsers");
        for (Map.Entry<String, Object> parser : parsersEntry.entrySet())
        {
            addParser(parser.getKey(), (Map<String, Object>) parser.getValue());
        }

        config = null;
    }

    @SuppressWarnings("unchecked")
    private void addProcessor(String name, Map<String, Object> config) throws ConfigException
    {
        logger.debug("Adding {} processor {}", name, config);

        IProcessor processor = moduleFactory.createProcessor(name, config);

        if (processor == null)
        {
            return;
        }

        processors.put(name, processor);
    }

    private void setupCompositeProcessor(IProcessor processor, Map<String, Object> config) throws ConfigException
    {
        if (!(processor instanceof ICompositeProcessor))
        {
            return;
        }

        for (String subProcessor : (List<String>) config.get("processors"))
        {
            if (processors.containsKey(subProcessor))
            {
                ((ICompositeProcessor) processor).addProcessor(processors.get(subProcessor));
            }
            else
            {
                throw new ConfigException(subProcessor + " processor does not exist!");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addParser(String name, Map<String, Object> config) throws ConfigException
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
    }

    public void handle(String line)
    {
        try
        {
            update();
        }
        catch (ConfigException e)
        {
            throw new RuntimeException("Updating module chain failed: " + e.getMessage());
        }

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

    public void stop()
    {
        for (IProcessor processor : processors.values())
        {
            processor.stop();
        }
    }

    private void reset()
    {
        parsers.clear();
        processParams.clear();
        transitions.clear();
        processors.clear();
    }

}
