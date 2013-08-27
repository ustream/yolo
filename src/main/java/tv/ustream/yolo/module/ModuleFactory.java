package tv.ustream.yolo.module;

import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigGroup;
import tv.ustream.yolo.module.parser.IParser;
import tv.ustream.yolo.module.parser.PassThruParser;
import tv.ustream.yolo.module.parser.RegexpParser;
import tv.ustream.yolo.module.processor.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
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
        config.addConfigValue("processParams", Map.class, false, null);
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

    private void setupModule(String name, IModule module, Map<String, Object> rawConfig) throws ConfigException
    {
        ConfigGroup moduleConfig = module.getModuleConfig();
        if (moduleConfig != null)
        {
            module.getModuleConfig().parseValues(name, rawConfig);
        }

        module.setUpModule(rawConfig);
    }

    public IProcessor createProcessor(String name, Map<String, Object> rawConfig) throws ConfigException
    {
        processorModuleConfig.parseValues(name, rawConfig);

        IProcessor processor = create((String) rawConfig.get("class"));

        setupModule(name, processor, rawConfig);

        return processor;
    }

    public IParser createParser(String name, Map<String, Object> rawConfig) throws ConfigException
    {
        parserModuleConfig.parseValues(name, rawConfig);

        IParser parser = create((String) rawConfig.get("class"));

        setupModule(name, parser, rawConfig);

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
            IProcessor module = factory.create(className);
            ConfigGroup config = getDefaultProcessorModuleConfig().merge(module.getModuleConfig());
            String usage = config.getUsageString("  - ");

            ConfigGroup processParamsConfig = module.getProcessParamsConfig();
            String usage2 = "";
            if (processParamsConfig != null && !processParamsConfig.isEmpty())
            {
                usage2 = "  - processParams:" + System.lineSeparator() + processParamsConfig.getUsageString("    - ");
            }

            System.out.format("* %s - %s%n%s%s%n", className, module.getModuleDescription(), usage, usage2);
        }

        System.out.println("Available parsers");
        System.out.println("-----------------");
        System.out.println();
        for (String className : availableParsers)
        {
            IParser module = factory.create(className);
            ConfigGroup config = getDefaultParserModuleConfig().merge(module.getModuleConfig());
            String usage = config.getUsageString("  - ");

            System.out.format("* %s - %s%n%s%n", className, module.getModuleDescription(), usage);
        }
    }

}
