package tv.ustream.yolo.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.config.ConfigPattern;
import tv.ustream.yolo.module.parser.IParser;
import tv.ustream.yolo.module.processor.ICompositeProcessor;
import tv.ustream.yolo.module.processor.IProcessor;
import tv.ustream.yolo.module.reader.IReader;
import tv.ustream.yolo.module.reader.IReaderListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class ModuleChain implements IReaderListener
{

    private static final Logger LOG = LoggerFactory.getLogger(ModuleChain.class);

    private final ModuleFactory moduleFactory;

    private final Map<String, IReader> readers = new HashMap<String, IReader>();

    private final Map<String, IParser> parsers = new HashMap<String, IParser>();

    private final Map<String, IProcessor> processors = new HashMap<String, IProcessor>();

    private final Map<String, Map<String, Map<String, Object>>> transitions =
            new HashMap<String, Map<String, Map<String, Object>>>();

    private Map<String, Object> rawConfig = null;

    public ModuleChain(final ModuleFactory moduleFactory)
    {
        this.moduleFactory = moduleFactory;
    }

    private ConfigMap getMainConfig()
    {
        ConfigMap mainConfig = new ConfigMap();
        mainConfig.addConfigValue("readers", Map.class);
        mainConfig.addConfigValue("processors", Map.class);
        mainConfig.addConfigValue("parsers", Map.class);
        return mainConfig;
    }

    public void updateConfig(final Map<String, Object> config, final boolean instant) throws ConfigException
    {
        rawConfig = config;

        getMainConfig().parse("[root]", rawConfig);

        if (instant)
        {
            update();
        }
    }

    private void update() throws ConfigException
    {
        if (rawConfig == null)
        {
            return;
        }

        stop();

        reset();

        addReaders((Map<String, Object>) rawConfig.get("readers"));

        addProcessors((Map<String, Object>) rawConfig.get("processors"));

        addParsers((Map<String, Object>) rawConfig.get("parsers"));

        rawConfig = null;
    }

    private void addReaders(Map<String, Object> readersConfig) throws ConfigException
    {
        for (Map.Entry<String, Object> reader : readersConfig.entrySet())
        {
            addReader(reader.getKey(), (Map<String, Object>) reader.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void addReader(String name, Map<String, Object> config) throws ConfigException
    {
        LOG.debug("Adding {} reader {}", name, config);

        IReader reader = moduleFactory.createReader(name, config);

        if (reader == null)
        {
            return;
        }

        reader.setReaderListener(this);

        readers.put(name, reader);
    }

    private void addProcessors(Map<String, Object> processorsConfig) throws ConfigException
    {
        for (Map.Entry<String, Object> processor : processorsConfig.entrySet())
        {
            addProcessor(processor.getKey(), (Map<String, Object>) processor.getValue());
        }

        for (Map.Entry<String, Object> processor : processorsConfig.entrySet())
        {
            setupCompositeProcessor(processors.get(processor.getKey()), (Map<String, Object>) processor.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void addProcessor(String name, Map<String, Object> config) throws ConfigException
    {
        LOG.info("Adding {} processor {}", name, config);

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

    private void addParsers(Map<String, Object> parsersConfig) throws ConfigException
    {
        for (Map.Entry<String, Object> parser : parsersConfig.entrySet())
        {
            addParser(parser.getKey(), (Map<String, Object>) parser.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void addParser(String name, Map<String, Object> config) throws ConfigException
    {
        LOG.info("Adding {} parser {}", name, config);

        IParser parser = moduleFactory.createParser(name, config);

        if (parser == null)
        {
            return;
        }

        parsers.put(name, parser);

        Map<String, Object> parserProcessors = (Map<String, Object>) config.get("processors");

        transitions.put(name, new HashMap<String, Map<String, Object>>());

        for (Map.Entry<String, Object> parserProcessor : parserProcessors.entrySet())
        {
            if (!processors.containsKey(parserProcessor.getKey()))
            {
                throw new ConfigException(parserProcessor.getKey() + " processor does not exist");
            }

            addTransition(name, parserProcessor.getKey(), parserProcessor.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void addTransition(String parserName, String processorName, Object params) throws ConfigException
    {
        ConfigMap processParamsConfig = processors.get(processorName).getProcessParamsConfig();
        if (processParamsConfig != null)
        {
            processParamsConfig.parse(parserName + ".processors." + processorName, params);
        }

        transitions.get(parserName).put(
                processorName,
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
                Map<String, Object> parserOutput = parsers.get(parserName).parse(line);
                if (parserOutput != null)
                {
                    match = true;

                    for (Map.Entry<String, Map<String, Object>> processor : transitions.get(parserName).entrySet())
                    {
                        processors.get(processor.getKey()).process(parserOutput, processor.getValue());
                    }
                }
            }
        }
    }

    public void start()
    {
        for (IReader reader : readers.values())
        {
            reader.start();
            Thread thread = new Thread(reader);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void stop()
    {
        for (IReader reader : readers.values())
        {
            reader.stop();
        }

        for (IProcessor processor : processors.values())
        {
            processor.stop();
        }
    }

    private void reset()
    {
        readers.clear();
        parsers.clear();
        transitions.clear();
        processors.clear();
    }

}
