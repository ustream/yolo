package tv.ustream.yolo;

import com.google.gson.Gson;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigPattern;
import tv.ustream.yolo.handler.FileHandler;
import tv.ustream.yolo.module.ModuleChain;
import tv.ustream.yolo.module.ModuleFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * @author bandesz
 */
public class Yolo
{

    private static final Logger logger = LoggerFactory.getLogger(Yolo.class);

    private final Options cliOptions = new Options();

    private Map<String, Object> config;

    private String configPath;

    private String filePath;

    private Boolean readWholeFile;

    private Boolean reopenFile;

    private long watchConfigInterval;

    private ModuleChain moduleChain = new ModuleChain(new ModuleFactory());

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

        Option watchConfigInterval = new Option("watchConfigInterval", true, "check config file periodically and update without stopping, default: 5 sec");
        watchConfigInterval.setArgName("second");
        cliOptions.addOption(watchConfigInterval);
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

        watchConfigInterval = Integer.parseInt(cli.getOptionValue("watchConfigInterval", "5")) * 1000;
    }

    @SuppressWarnings("unchecked")
    private void readConfig(boolean update) throws ConfigException
    {
        try
        {
            config = (Map<String, Object>) new Gson().fromJson(new FileReader(configPath), Map.class);
        }
        catch (Exception e)
        {
            exitWithError("Failed to open configuration file: " + e.getMessage(), false);
        }

        moduleChain.updateConfig(config, !update);
    }

    private void observeConfigChanges() throws Exception
    {
        if (watchConfigInterval < 0)
        {
            return;
        }

        FileAlterationListenerAdaptor listener = new FileAlterationListenerAdaptor()
        {
            @Override
            public void onFileChange(File file)
            {
                logger.debug("Config file changed: {}", configPath);
                try
                {
                    readConfig(true);
                }
                catch (ConfigException e)
                {
                    exitWithError("Failed to refresh config: " + e.getMessage(), false);
                }
            }
        };

        String filename = configPath.substring(configPath.lastIndexOf('/') + 1);
        String directory = configPath.substring(0, configPath.lastIndexOf('/'));

        FileAlterationObserver observer = new FileAlterationObserver(new File(directory), FileFilterUtils.nameFileFilter(filename));
        observer.addListener(listener);

        FileAlterationMonitor monitor = new FileAlterationMonitor(watchConfigInterval);
        monitor.addObserver(observer);

        monitor.start();
    }

    private void setGlobalParameters() throws Exception
    {
        ConfigPattern.addGlobalParameter("HOSTNAME", getHostname());
    }

    private String getHostname() throws IOException
    {
        Process process = Runtime.getRuntime().exec("hostname -s");
        return new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
    }

    private void startFileHandler()
    {
        fileHandler = new FileHandler(moduleChain, filePath, 1000, readWholeFile, reopenFile);

        fileHandler.start();
    }

    public void start(String[] args)
    {
        try
        {
            setGlobalParameters();

            parseCliOptions(args);

            readConfig(false);

            observeConfigChanges();

            startFileHandler();

            addShutdownHook();
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

    public void addShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                logger.debug("Shutting down..");
                Yolo.this.stop();
            }
        });
    }

    public void stop()
    {
        if (null != fileHandler)
        {
            fileHandler.stop();
        }
        moduleChain.stop();
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
