package com.ustream.loggy.handler;

import com.ustream.loggy.config.ConfigException;
import com.ustream.loggy.config.ConfigGroup;
import com.ustream.loggy.config.ConfigPattern;
import com.ustream.loggy.config.ConfigUtils;
import com.ustream.loggy.module.ModuleFactory;
import com.ustream.loggy.module.parser.IParser;
import com.ustream.loggy.module.processor.ICompositeProcessor;
import com.ustream.loggy.module.processor.IProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class DataHandler implements ILineHandler
{

    private final ModuleFactory moduleFactory;

    private Boolean debug;

    private final Map<String, IParser> parsers = new HashMap<String, IParser>();

    private final Map<String, Map<String, Object>> processorParams = new HashMap<String, Map<String, Object>>();

    private final Map<String, IProcessor> processors = new HashMap<String, IProcessor>();

    private final Map<String, String> transitions = new HashMap<String, String>();

    public DataHandler(ModuleFactory moduleFactory, Boolean debug)
    {
        this.moduleFactory = moduleFactory;
        this.debug = debug;
    }

    public void addProcessor(String name, Object data) throws Exception
    {
        if (debug)
        {
            System.out.format("Adding %s processor %s\n", name, data);
        }

        Map<String, Object> config = ConfigUtils.castObjectMap(data);

        IProcessor processor = moduleFactory.createProcessor(name, config, debug);
        processors.put(name, processor);

        if (processor instanceof ICompositeProcessor)
        {
            setupCompositeProcessor((ICompositeProcessor) processor, ConfigUtils.castStringList(config.get("processors")));
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

    public void addParser(String name, Object data) throws Exception
    {
        if (debug)
        {
            System.out.format("Adding %s parser %s\n", name, data);
        }

        Map<String, Object> config = ConfigUtils.castObjectMap(data);

        IParser parser = moduleFactory.createParser(name, config, debug);
        parsers.put(name, parser);

        String processorName = (String) config.get("processor");

        if (!processors.containsKey(processorName))
        {
            throw new ConfigException(processorName + " processor does not exist");
        }

        Map<String, Object> params = ConfigUtils.castObjectMap(config.get("processorParams"));

        if (params != null)
        {
            ConfigGroup processorParamsConfig = processors.get(processorName).getProcessorParamsConfig();
            if (processorParamsConfig != null)
            {
                processorParamsConfig.parseValues(name + ".processorParams", params);
            }

            processorParams.put(name, ConfigPattern.processMap(params));

            processors.get(processorName).validateProcessorParams(parser.getOutputParameters(), params);
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
                Map<String, String> parserParams = parsers.get(parserName).parse(line);
                if (parserParams != null)
                {
                    match = true;
                    processors.get(transitions.get(parserName)).process(parserParams, processorParams.get(parserName));
                }
            }
        }
    }

}
