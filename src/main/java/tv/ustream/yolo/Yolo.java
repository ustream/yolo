package tv.ustream.yolo;

import com.google.gson.Gson;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigGroup;
import tv.ustream.yolo.handler.FileHandler;
import tv.ustream.yolo.module.ModuleChain;
import tv.ustream.yolo.module.ModuleFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * @author bandesz
 */
public class Yolo
{

    private final Logger logger = LoggerFactory.getLogger(Yolo.class);

    private final Options cliOptions = new Options();

    private Map<String, Object> config;

    private String configPath;

    private String filePath;

    private Boolean readWholeFile;

    private Boolean reopenFile;

    private ModuleChain moduleChain;

    private FileHandler fileHandler;

    public Yolo()
    {
        buildCliOptions();
    }

    private void buildCliOptions()
    {
        cliOptions.addOption("help", false, "print this message");

        Option file = new Option("file", true, "path to logfile");
        file.setArgName("path");
        cliOptions.addOption(file);

        Option config = new Option("config", true, "path to config file");
        config.setArgName("path");
        cliOptions.addOption(config);

        cliOptions.addOption("whole", false, "tail file from the beginning");

        cliOptions.addOption("reopen", false, "reopen file between reading the chunks");

        cliOptions.addOption("listModules", false, "list available modules");
    }

    private void parseCliOptions(String[] args) throws Exception
    {
        CommandLine cli;
        try
        {
            cli = new PosixParser().parse(cliOptions, args);
        }
        catch (ParseException exp)
        {
            exitWithError("Error: " + exp.getMessage(), true);
            return;
        }

        if (cli.hasOption("help"))
        {
            printHelp();
            System.exit(0);
        }

        if (cli.hasOption("listModules"))
        {
            ModuleFactory.printAvailableModules();
            System.exit(0);
        }

        configPath = cli.getOptionValue("config");

        if (null == configPath || configPath.isEmpty())
        {
            exitWithError("config parameter is missing!", true);
        }

        filePath = cli.getOptionValue("file");
        if (null == filePath || filePath.isEmpty())
        {
            exitWithError("file parameter is missing!", true);
        }

        readWholeFile = cli.hasOption("whole");

        reopenFile = cli.hasOption("reopen");
    }

    private ConfigGroup getMainConfig()
    {
        ConfigGroup config = new ConfigGroup();
        config.addConfigValue("processors", Map.class);
        config.addConfigValue("parsers", Map.class);
        return config;
    }

    @SuppressWarnings("unchecked")
    private void readConfig() throws ConfigException
    {
        try
        {
            config = (Map<String, Object>) new Gson().fromJson(new FileReader(configPath), Map.class);
        }
        catch (IOException e)
        {
            exitWithError("Failed to open configuration file: " + e.getMessage(), false);
        }

        getMainConfig().parseValues("[root]", config);
    }

    @SuppressWarnings("unchecked")
    private void initModuleChain() throws Exception
    {
        moduleChain = new ModuleChain(new ModuleFactory());

        try
        {
            Map<String, Object> processors = (Map<String, Object>) config.get("processors");
            for (Map.Entry<String, Object> processor : processors.entrySet())
            {
                moduleChain.addProcessor(processor.getKey(), (Map<String, Object>) processor.getValue());
            }

            Map<String, Object> parsers = (Map<String, Object>) config.get("parsers");
            for (Map.Entry<String, Object> parser : parsers.entrySet())
            {
                moduleChain.addParser(parser.getKey(), (Map<String, Object>) parser.getValue());
            }
        }
        catch (ConfigException e)
        {
            exitWithError(e.getMessage(), false);
        }
    }

    private void startFileHandler()
    {
        fileHandler = new FileHandler(moduleChain, filePath, readWholeFile, reopenFile);

        fileHandler.start();
    }

    public void start(String[] args)
    {
        try
        {
            parseCliOptions(args);

            readConfig();

            initModuleChain();

            startFileHandler();
        }
        catch (ConfigException e)
        {
            System.out.println(e.getMessage());
        }
        catch (Exception e)
        {
            if (!e.getMessage().isEmpty())
            {
                logger.error(e.getMessage());
            }
            else
            {
                e.printStackTrace();
            }
        }
    }

    public void stop()
    {
        if (null != fileHandler)
        {
            fileHandler.stop();
        }
    }

    private void printHelp()
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("yolo", cliOptions);
    }

    private void exitWithError(String message, Boolean printHelp)
    {
        System.out.println(message);
        if (printHelp)
        {
            printHelp();
        }
        System.exit(1);
    }

    public static void main(String[] args)
    {
        Yolo yolo = new Yolo();
        yolo.start(args);
    }

}
