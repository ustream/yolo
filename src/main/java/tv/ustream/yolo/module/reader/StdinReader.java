package tv.ustream.yolo.module.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.config.ConfigMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * @author bandesz
 */
public class StdinReader implements IReader, Runnable
{

    private static final Logger LOG = LoggerFactory.getLogger(StdinReader.class);

    private IReaderListener listener;

    private boolean running = false;

    @Override
    public void start()
    {
        running = true;
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop()
    {
        running = false;
    }

    @Override
    public void run()
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String input;

        while (running)
        {
            try
            {
                input = br.readLine();
                if (input != null)
                {
                    listener.handle(input);
                }
                else
                {
                    Thread.sleep(1000);
                }
            }
            catch (IOException e)
            {
                LOG.debug("Error reading from standard input: {}", e.getMessage());
            }
            catch (InterruptedException e)
            {
                LOG.debug("{} interrupted", this.getClass().getCanonicalName());
            }
        }
        try
        {
            br.close();
        }
        catch (IOException e)
        {
            LOG.debug("Error closing standard input: {}", e.getMessage());
        }
    }

    @Override
    public void setReaderListener(final IReaderListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void setUpModule(final Map<String, Object> parameters)
    {
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        return null;
    }

    @Override
    public String getModuleDescription()
    {
        return "Reads data from standard input";
    }

}
