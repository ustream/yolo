package tv.ustream.yolo.handler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author bandesz
 */
public class FileHandler implements TailerListener
{

    private static final Logger LOG = LoggerFactory.getLogger(FileHandler.class);

    private static final Pattern WILDCARD_PATTERN = Pattern.compile("[\\?\\*]+");

    private final String filePath;

    private boolean dynamicFilename;

    private final long delayMs;

    private final boolean readWhole;

    private final boolean reopen;

    private Tailer tailer;

    private final ILineHandler lineProcessor;

    public FileHandler(
            final ILineHandler lineProcessor,
            final String filePath,
            final long delayMs,
            final boolean readWhole,
            final boolean reopen
    )
    {
        this.lineProcessor = lineProcessor;
        this.filePath = filePath;
        this.dynamicFilename = WILDCARD_PATTERN.matcher(filePath).find();
        this.delayMs = delayMs;
        this.readWhole = readWhole;
        this.reopen = reopen;
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

        tailer = new Tailer(file, this, delayMs, !readWhole, reopen);

        Thread thread = new Thread(tailer);
        thread.setName("Tailer");
        thread.start();
    }

    public void stop()
    {
        tailer.stop();
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
            lineProcessor.handle(line);
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
}
