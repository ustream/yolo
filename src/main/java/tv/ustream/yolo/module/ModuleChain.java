package tv.ustream.yolo.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.config.ConfigPattern;
import tv.ustream.yolo.config.file.IConfigFileListener;
import tv.ustream.yolo.module.parser.IParser;
import tv.ustream.yolo.module.processor.ICompositeProcessor;
import tv.ustream.yolo.module.processor.IProcessor;
import tv.ustream.yolo.module.reader.IReader;
import tv.ustream.yolo.module.reader.IReaderListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author bandesz
 */
public class ModuleChain implements IReaderListener, IConfigFileListener
{

    private static final Logger LOG = LoggerFactory.getLogger(ModuleChain.class);

    private final ModuleFactory moduleFactory;

    private final ModuleStore<IReader> readers = new ModuleStore<IReader>();

    private final ModuleStore<IParser> parsers = new ModuleStore<IParser>();

    private final ModuleStore<IProcessor> processors = new ModuleStore<IProcessor>();

    private final Map<IParser, Map<IProcessor, Map<String, Object>>> transitions =
            new HashMap<IParser, Map<IProcessor, Map<String, Object>>>();

    private ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();

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


    @Override
    public void configChanged(final String namespace, final Map<String, Object> config, final boolean update)
            throws ConfigException
    {
        getMainConfig().parse("[root]", config);

        updateLock.writeLock().lock();
        try
        {
            removeModules(namespace);

            addReaders(namespace, (Map<String, Object>) config.get("readers"));

            addProcessors(namespace, (Map<String, Object>) config.get("processors"));

            addParsers(namespace, (Map<String, Object>) config.get("parsers"));
        }
        finally
        {
            updateLock.writeLock().unlock();
        }
    }

    private void addReaders(String namespace, Map<String, Object> readersConfig) throws ConfigException
    {
        for (Map.Entry<String, Object> reader : readersConfig.entrySet())
        {
            addReader(namespace, reader.getKey(), (Map<String, Object>) reader.getValue());
        }
    }

    private void addReader(String namespace, String name, Map<String, Object> config) throws ConfigException
    {
        LOG.debug("Adding {}.{} reader {}", namespace, name, config);

        IReader reader = moduleFactory.createReader(name, config);

        if (reader == null)
        {
            return;
        }

        reader.setReaderListener(this);

        readers.add(namespace, name, reader);

        reader.start();
        Thread thread = new Thread(reader);
        thread.setDaemon(true);
        thread.start();
    }

    private void addProcessors(String namespace, Map<String, Object> processorsConfig) throws ConfigException
    {
        for (Map.Entry<String, Object> processor : processorsConfig.entrySet())
        {
            addProcessor(namespace, processor.getKey(), (Map<String, Object>) processor.getValue());
        }

        for (Map.Entry<String, Object> processor : processorsConfig.entrySet())
        {
            setupCompositeProcessor(namespace, processors.get(processor.getKey(), namespace),
                    (Map<String, Object>) processor.getValue());
        }
    }

    private void addProcessor(String namespace, String name, Map<String, Object> config) throws ConfigException
    {
        LOG.debug("Adding {}.{} processor {}", namespace, name, config);

        IProcessor processor = moduleFactory.createProcessor(name, config);

        if (processor == null)
        {
            return;
        }

        processors.add(namespace, name, processor);
    }

    private void setupCompositeProcessor(String namespace, IProcessor processor, Map<String, Object> config)
            throws ConfigException
    {
        if (!(processor instanceof ICompositeProcessor))
        {
            return;
        }

        for (String subProcessor : (List<String>) config.get("processors"))
        {
            if (processors.contains(subProcessor, namespace))
            {
                ((ICompositeProcessor) processor).addProcessor(processors.get(subProcessor, namespace));
            }
            else
            {
                throw new ConfigException(subProcessor + " processor does not exist!");
            }
        }
    }

    private void addParsers(String namespace, Map<String, Object> parsersConfig) throws ConfigException
    {
        for (Map.Entry<String, Object> parser : parsersConfig.entrySet())
        {
            addParser(namespace, parser.getKey(), (Map<String, Object>) parser.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void addParser(String namespace, String name, Map<String, Object> config) throws ConfigException
    {
        LOG.debug("Adding {}.{} parser {}", namespace, name, config);

        IParser parser = moduleFactory.createParser(name, config);

        if (parser == null)
        {
            return;
        }

        parsers.add(namespace, name, parser);

        Map<String, Object> parserProcessors = (Map<String, Object>) config.get("processors");

        transitions.put(parser, new HashMap<IProcessor, Map<String, Object>>());

        for (Map.Entry<String, Object> parserProcessor : parserProcessors.entrySet())
        {
            if (!processors.contains(parserProcessor.getKey(), namespace))
            {
                throw new ConfigException(parserProcessor.getKey() + " processor does not exist");
            }

            addTransition(namespace, name, parserProcessor.getKey(), parserProcessor.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void addTransition(String namespace, String parserName, String processorName, Object params)
            throws ConfigException
    {
        ConfigMap processParamsConfig = processors.get(processorName, namespace).getProcessParamsConfig();
        if (processParamsConfig != null)
        {
            processParamsConfig.parse(parserName + ".processors." + processorName, params);
        }

        IParser parser = parsers.get(parserName, namespace);

        transitions.get(parser).put(
                processors.get(processorName, namespace),
                (Map<String, Object>) ConfigPattern.replacePatterns(params, parser.getOutputKeys())
        );
    }

    public synchronized void handle(String line)
    {
        updateLock.readLock().lock();

        try
        {
            Boolean match = false;
            for (Map.Entry<String, IParser> parserEntry : parsers.entrySet())
            {
                if (!match || parserEntry.getValue().runAlways())
                {
                    Map<String, String> parserOutput = parserEntry.getValue().parse(line);
                    if (parserOutput != null)
                    {
                        match = true;
                        process(parserEntry.getValue(), parserOutput);
                    }
                }
            }
        }
        finally
        {
            updateLock.readLock().unlock();
        }
    }

    private void process(IParser parser, Map<String, String> parserOutput)
    {
        for (Map.Entry<IProcessor, Map<String, Object>> processorEntry : transitions.get(parser).entrySet())
        {
            synchronized (processorEntry.getKey())
            {
                processorEntry.getKey().process(parserOutput, processorEntry.getValue());
            }
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

    private void removeModules(String namespace)
    {
        for (Map.Entry<String, IReader> readerEntry : readers.namespaceEntrySet(namespace))
        {
            readerEntry.getValue().stop();
            readers.remove(readerEntry.getKey());
        }

        for (Map.Entry<String, IParser> parserEntry : parsers.namespaceEntrySet(namespace))
        {
            parsers.remove(parserEntry.getKey());
            transitions.remove(parserEntry.getValue());
        }

        for (Map.Entry<String, IProcessor> processorEntry : processors.namespaceEntrySet(namespace))
        {
            processors.remove(processorEntry.getKey());
        }
    }

}
