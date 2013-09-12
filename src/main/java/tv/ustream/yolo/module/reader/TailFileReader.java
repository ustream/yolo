package tv.ustream.yolo.module.reader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.io.TailerFile;
import tv.ustream.yolo.config.ConfigMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author bandesz
 */
public class TailFileReader implements IReader, TailerListener
{

    private static final Logger LOG = LoggerFactory.getLogger(TailFileReader.class);

    private static final Pattern WILDCARD_PATTERN = Pattern.compile("[\\?\\*]+");

    private String filePath;

    private boolean dynamicFilename;

    private long delayMs;

    private boolean readWhole;

    private boolean reopen;

    private Tailer tailer;

    private IReaderListener listener;

    @Override
    public void setUpModule(final Map<String, Object> parameters)
    {
        filePath = (String) parameters.get("file");
        this.dynamicFilename = WILDCARD_PATTERN.matcher(filePath).find();
        delayMs = ((Number) parameters.get("delayMs")).longValue();
        readWhole = (Boolean) parameters.get("readWhole");
        reopen = (Boolean) parameters.get("reopen");
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        config.addConfigValue("file", String.class);
        config.addConfigValue("delayMs", Number.class, false, 1000);
        config.addConfigValue("readWhole", Boolean.class, false, false);
        config.addConfigValue("reopen", Boolean.class, false, false);
        return config;
    }

    @Override
    public String getModuleDescription()
    {
        return "File tailer";
    }

    private File findFile()
    {
        String filename = filePath.substring(filePath.lastIndexOf('/') + 1);

        File directory = new File(filePath.substring(0, filePath.lastIndexOf('/')));

        Collection<File> files = FileUtils.listFiles(
                directory,
                new WildcardFileFilter(filename),
                FalseFileFilter.INSTANCE
        );

        if (!files.isEmpty())
        {
            return files.iterator().next();
        }
        else
        {
            return null;
        }
    }

    public void start()
    {
        File file;
        do
        {
            file = findFile();
            if (file == null)
            {
                try
                {
                    TimeUnit.SECONDS.sleep(1);
                }
                catch (InterruptedException ignored)
                {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        while (file == null);

        LOG.info("Tailing file {}", file.getAbsolutePath());

        tailer = new Tailer(TailerFile.create(file), this, delayMs, !readWhole, reopen);
    }

    public void stop()
    {
        if (tailer != null)
        {
            tailer.stop();
            tailer = null;
        }
    }

    @Override
    public void setReaderListener(final IReaderListener listener)
    {
        this.listener = listener;
    }

    private void restart()
    {
        stop();
        start();
    }

    @Override
    public void init(final Tailer tailer)
    {
    }

    @Override
    public void fileNotFound()
    {
        if (dynamicFilename)
        {
            restart();
            return;
        }

        LOG.error("Tailer error: file not found: {}", tailer.getFile().getAbsolutePath());

        try
        {
            TimeUnit.SECONDS.sleep(1);
        }
        catch (InterruptedException ignored)
        {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void fileRotated()
    {
        LOG.info("Tailer: file was rotated: {}", tailer.getFile().getAbsolutePath());

        try
        {
            TimeUnit.SECONDS.sleep(1);
        }
        catch (InterruptedException ignored)
        {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void handle(final String line)
    {
        try
        {
            listener.handle(line);
        }
        catch (Exception e)
        {
            LOG.error("Line processing error: {} - {} ", e.getClass().getName(), e.getMessage());
        }
    }

    @Override
    public void handle(final Exception ex)
    {
        if (ex instanceof FileNotFoundException && dynamicFilename)
        {
            restart();
            return;
        }

        LOG.error("Tailer error: {}", ex.getMessage());

        try
        {
            TimeUnit.SECONDS.sleep(1);
        }
        catch (InterruptedException ignored)
        {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run()
    {
        tailer.run();
    }
}
