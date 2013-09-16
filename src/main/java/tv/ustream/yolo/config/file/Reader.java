package tv.ustream.yolo.config.file;

import com.google.gson.Gson;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.config.ConfigException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * @author bandesz
 */
public abstract class Reader
{

    private static final Logger LOG = LoggerFactory.getLogger(Reader.class);

    private final File file;

    private long watchInterval;

    private IConfigFileListener listener;

    private FileAlterationMonitor monitor;

    public Reader(final File file, final long watchInterval, final IConfigFileListener listener) throws IOException
    {
        if (!file.exists())
        {
            throw new IOException("Config file or directory does not exist: " + file.getAbsolutePath());
        }

        this.file = file;
        this.watchInterval = watchInterval;
        this.listener = listener;
    }

    public void start() throws Exception
    {
        readConfig();

        if (watchInterval > 0)
        {
            setupConfigMonitor();
        }
    }

    public void stop() throws Exception
    {
        if (monitor != null)
        {
            monitor.stop();
            monitor = null;
        }
    }

    protected abstract void readConfig();

    protected void readFile(final File file, final boolean first)
    {
        String namespace = file.getName().replace(".json", "");
        try
        {
            Map<String, Object> config = null;
            if (file.exists())
            {
                config = (Map<String, Object>) new Gson().fromJson(new FileReader(file), Map.class);
            }
            listener.configChanged(namespace, config, !first);
        }
        catch (IOException e)
        {
            LOG.error("Failed to read config file: {}", file.getAbsolutePath());
        }
        catch (ConfigException e)
        {
            LOG.error("Configuration error: {}", e.getMessage());
        }
    }

    protected abstract FileAlterationObserver createObserver();

    private void setupConfigMonitor() throws Exception
    {
        FileAlterationListenerAdaptor fileAlterationListener = new FileAlterationListenerAdaptor()
        {
            @Override
            public void onFileCreate(final File file)
            {
                LOG.debug("Config file created: {}", file.getAbsolutePath());

                readFile(file, false);
            }

            @Override
            public void onFileChange(final File file)
            {
                LOG.debug("Config file changed: {}", file.getAbsolutePath());

                readFile(file, false);
            }

            @Override
            public void onFileDelete(final File file)
            {
                LOG.debug("Config file deleted: {}", file.getAbsolutePath());

                readFile(file, false);
            }
        };

        FileAlterationObserver observer = createObserver();
        observer.addListener(fileAlterationListener);

        monitor = new FileAlterationMonitor(watchInterval);
        monitor.addObserver(observer);

        monitor.start();
    }

    protected File getFile()
    {
        return file;
    }

}
