package com.ustream.loggy.module;

import com.ustream.loggy.config.ConfigException;
import com.ustream.loggy.config.ConfigGroup;
import com.ustream.loggy.module.parser.IParser;
import com.ustream.loggy.module.parser.PassThruParser;
import com.ustream.loggy.module.parser.RegexpParser;
import com.ustream.loggy.module.processor.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ModuleFactory
{

    private static final List<String> availableProcessors = Arrays.asList(
        CompositeProcessor.class.getCanonicalName(),
        ConsoleProcessor.class.getCanonicalName(),
        NoOpProcessor.class.getCanonicalName(),
        StatsDProcessor.class.getCanonicalName()
    );

    private static final List<String> availableParsers = Arrays.asList(
        PassThruParser.class.getCanonicalName(),
        RegexpParser.class.getCanonicalName()
    );

    private static final ConfigGroup processorModuleConfig = getDefaultProcessorModuleConfig();

    private static final ConfigGroup parserModuleConfig = getDefaultParserModuleConfig();

    private static ConfigGroup getDefaultProcessorModuleConfig()
    {
        ConfigGroup config = new ConfigGroup();
        config.addConfigValue("class", String.class);
        return config;
    }

    private static ConfigGroup getDefaultParserModuleConfig()
    {
        ConfigGroup config = new ConfigGroup();
        config.addConfigValue("class", String.class);
        config.addConfigValue("processor", String.class);
        config.addConfigValue("processorParams", Map.class, false, null);
        return config;
    }

    @SuppressWarnings("unchecked")
    private <T extends IModule> T create(String className) throws ConfigException
    {
        try
        {
            return (T) Class.forName(className).newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            throw new ConfigException("failed to load class: " + className);
        }
    }

    private void setupModule(String name, IModule module, Map<String, Object> rawConfig, boolean debug) throws ConfigException
    {
        ConfigGroup moduleConfig = module.getModuleConfig();
        if (moduleConfig != null)
        {
            module.getModuleConfig().validate(name, rawConfig);
        }

        module.setUpModule(rawConfig, debug);
    }

    public IProcessor createProcessor(String name, Map<String, Object> rawConfig, boolean debug) throws ConfigException
    {
        processorModuleConfig.validate(name, rawConfig);

        IProcessor processor = create((String) rawConfig.get("class"));

        setupModule(name, processor, rawConfig, debug);

        return processor;
    }

    public IParser createParser(String name, Map<String, Object> rawConfig, boolean debug) throws ConfigException
    {
        parserModuleConfig.validate(name, rawConfig);

        IParser parser = create((String) rawConfig.get("class"));

        setupModule(name, parser, rawConfig, debug);

        return parser;
    }

    public static void printAvailableModules() throws ConfigException
    {
        ModuleFactory factory = new ModuleFactory();
        System.out.println("Available processors");
        System.out.println("--------------------");
        System.out.println();
        for (String className : availableProcessors)
        {
            IModule module = factory.create(className);
            ConfigGroup config = module.getModuleConfig();
            String usage = config != null ? "\n" + config.getUsageString("      ") : "";

            System.out.format("· %s - %s\n%s\n", className, module.getModuleDescription(), usage);
        }
        System.out.println();

        System.out.println("Available parsers");
        System.out.println("-----------------");
        System.out.println();
        for (String className : availableParsers)
        {
            IModule module = factory.create(className);
            ConfigGroup config = module.getModuleConfig();
            String usage = config != null ? "\n" + config.getUsageString("      ") : "";

            System.out.format("· %s - %s\n%s\n", className, module.getModuleDescription(), usage);
        }
        System.out.println("");
    }

}
