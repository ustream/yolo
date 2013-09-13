package tv.ustream.yolo.module.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.config.ConfigMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author bandesz
 */
public class ProcessReader implements IReader
{

    private static final Logger LOG = LoggerFactory.getLogger(ProcessReader.class);

    private final Runtime runtime = Runtime.getRuntime();

    private String command;

    private long frequencySec;

    private boolean running = false;

    private IReaderListener listener;

    @Override
    public void start()
    {
        running = true;
    }

    @Override
    public void stop()
    {
        running = false;
    }

    @Override
    public void setReaderListener(final IReaderListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void setUpModule(final Map<String, Object> parameters)
    {
        command = (String) parameters.get("command");
        frequencySec = ((Number) parameters.get("frequencySec")).longValue();
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("command", String.class);
        config.addConfigValue("frequencySec", Number.class, false, 60);
        return config;
    }

    @Override
    public String getModuleDescription()
    {
        return "Run a command and process result repeatedly";
    }

    @Override
    public void run()
    {
        while (running)
        {
            List<String> result = new ArrayList<String>();

            try
            {
                Process process = runtime.exec(command);
                BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                String line;

                line = error.readLine();
                while (line != null)
                {
                    LOG.error("Error: {}", line);
                    line = error.readLine();
                }

                line = output.readLine();
                while (line != null)
                {
                    result.add(line);
                    line = output.readLine();
                }

                int exitVal = process.waitFor();

                if (exitVal == 0)
                {
                    for (String data : result)
                    {
                        listener.handle(data);
                    }
                }
                else
                {
                    LOG.error("Exit code was: {}", exitVal);
                }
            }
            catch (IOException e)
            {
                LOG.error("Failed to execute command: {}, error: {}", command, e.getMessage());
            }
            catch (InterruptedException e)
            {
                LOG.info("{} interrupted", this.getClass().getCanonicalName());
            }
            finally
            {
                try
                {
                    TimeUnit.SECONDS.sleep(frequencySec);
                }
                catch (InterruptedException e)
                {
                    LOG.info("{} interrupted", this.getClass().getCanonicalName());
                }
            }
        }
    }

}
