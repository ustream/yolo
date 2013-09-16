package tv.ustream.yolo.module;

import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.module.parser.IParser;
import tv.ustream.yolo.module.parser.JsonParser;
import tv.ustream.yolo.module.parser.PassThruParser;
import tv.ustream.yolo.module.parser.RegexpParser;
import tv.ustream.yolo.module.parser.ScriptEngineParser;
import tv.ustream.yolo.module.processor.CompositeProcessor;
import tv.ustream.yolo.module.processor.ConsoleProcessor;
import tv.ustream.yolo.module.processor.graphite.GraphiteProcessor;
import tv.ustream.yolo.module.processor.IProcessor;
import tv.ustream.yolo.module.processor.StatsDProcessor;
import tv.ustream.yolo.module.reader.IReader;
import tv.ustream.yolo.module.reader.StdinReader;
import tv.ustream.yolo.module.reader.TailFileReader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class ModuleFactory
{

    private static final List<String> AVAILABLE_READERS = Arrays.asList(
            StdinReader.class.getCanonicalName(),
            TailFileReader.class.getCanonicalName()
    );

    private static final List<String> AVAILABLE_PROCESSORS = Arrays.asList(
            CompositeProcessor.class.getCanonicalName(),
            ConsoleProcessor.class.getCanonicalName(),
            GraphiteProcessor.class.getCanonicalName(),
            StatsDProcessor.class.getCanonicalName()
    );

    private static final List<String> AVAILABLE_PARSERS = Arrays.asList(
            PassThruParser.class.getCanonicalName(),
            RegexpParser.class.getCanonicalName(),
            JsonParser.class.getCanonicalName(),
            ScriptEngineParser.class.getCanonicalName()
    );

    private static final ConfigMap READER_MODULE_CONFIG = getDefaultReaderModuleConfig();

    private static final ConfigMap PROCESSOR_MODULE_CONFIG = getDefaultProcessorModuleConfig();

    private static final ConfigMap PARSER_MODULE_CONFIG = getDefaultParserModuleConfig();

    private static ConfigMap getDefaultReaderModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("class", String.class);
        return config;
    }

    private static ConfigMap getDefaultProcessorModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("class", String.class);
        return config;
    }

    private static ConfigMap getDefaultParserModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("class", String.class);
        config.addConfigValue("enabled", Boolean.class, false, true);
        config.addConfigValue("processors", Map.class);
        return config;
    }

    @SuppressWarnings("unchecked")
    private <T extends IModule> T create(final String className) throws ConfigException
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
        ConfigMap moduleConfig = module.getModuleConfig();
        if (moduleConfig != null)
        {
            module.getModuleConfig().parse(name, rawConfig);
        }

        module.setUpModule(rawConfig);
    }

    public IReader createReader(String name, Map<String, Object> rawConfig) throws ConfigException
    {
        READER_MODULE_CONFIG.parse(name, rawConfig);

        IReader reader = create((String) rawConfig.get("class"));

        setupModule(name, reader, rawConfig);

        return reader;
    }

    public IProcessor createProcessor(String name, Map<String, Object> rawConfig) throws ConfigException
    {
        PROCESSOR_MODULE_CONFIG.parse(name, rawConfig);

        IProcessor processor = create((String) rawConfig.get("class"));

        setupModule(name, processor, rawConfig);

        return processor;
    }

    public IParser createParser(String name, Map<String, Object> rawConfig) throws ConfigException
    {
        PARSER_MODULE_CONFIG.parse(name, rawConfig);

        if (!(Boolean) rawConfig.get("enabled"))
        {
            return null;
        }

        IParser parser = create((String) rawConfig.get("class"));

        setupModule(name, parser, rawConfig);

        return parser;
    }

    public static void printAvailableModules() throws ConfigException
    {
        ModuleFactory factory = new ModuleFactory();

        System.out.println("Available readers");
        System.out.println("--------------------");
        System.out.println();
        for (String className : AVAILABLE_READERS)
        {
            IReader module = factory.create(className);
            ConfigMap config = getDefaultReaderModuleConfig().merge(module.getModuleConfig());
            String usage = "  - params: " + config.getDescription("    ");

            System.out.format("* %s - %s%n%s%n", className, module.getModuleDescription(), usage);
        }

        System.out.println("Available processors");
        System.out.println("--------------------");
        System.out.println();
        for (String className : AVAILABLE_PROCESSORS)
        {
            IProcessor module = factory.create(className);
            ConfigMap config = getDefaultProcessorModuleConfig().merge(module.getModuleConfig());
            String usage = "  - params: " + config.getDescription("    ");

            ConfigMap processParamsConfig = module.getProcessParamsConfig();
            String usage2 = "";
            if (processParamsConfig != null && !processParamsConfig.isEmpty())
            {
                usage2 = "  - parser params: " + processParamsConfig.getDescription("    ");
            }

            System.out.format("* %s - %s%n%s%s%n", className, module.getModuleDescription(), usage, usage2);
        }

        System.out.println("Available parsers");
        System.out.println("-----------------");
        System.out.println();
        for (String className : AVAILABLE_PARSERS)
        {
            IParser module = factory.create(className);
            ConfigMap config = getDefaultParserModuleConfig().merge(module.getModuleConfig());
            String usage = "  - params: " + config.getDescription("    ");

            System.out.format("* %s - %s%n%s%n", className, module.getModuleDescription(), usage);
        }
    }

}
