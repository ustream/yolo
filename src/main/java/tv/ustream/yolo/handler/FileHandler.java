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
import java.util.regex.Pattern;

/**
 * @author bandesz
 */
public class FileHandler implements TailerListener
{

    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);

    private static final Pattern wildCardPattern = Pattern.compile("[\\?\\*]+");

    private final String filePath;

    private boolean dynamicFilename;

    private final long delayMs;

    private final boolean readWhole;

    private final boolean reopen;

    private Tailer tailer;

    private final ILineHandler lineProcessor;

    public FileHandler(ILineHandler lineProcessor, String filePath, long delayMs, boolean readWhole, boolean reopen)
    {
        this.lineProcessor = lineProcessor;
        this.filePath = filePath;
        this.dynamicFilename = wildCardPattern.matcher(filePath).find();
        this.delayMs = delayMs;
        this.readWhole = readWhole;
        this.reopen = reopen;
    }

    private File findFile()
    {
        String filename = filePath.substring(filePath.lastIndexOf('/') + 1);

        File directory = new File(filePath.substring(0, filePath.lastIndexOf('/')));

        Collection<File> files = FileUtils.listFiles(directory, new WildcardFileFilter(filename), FalseFileFilter.INSTANCE);

        return !files.isEmpty() ? files.iterator().next() : null;
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
                    Thread.sleep(1000);
                }
                catch (InterruptedException ignored)
                {
                }
            }
        }
        while (file == null);

        logger.debug("Tailing file {}", file.getAbsolutePath());

        tailer = new Tailer(file, this, delayMs, !readWhole, reopen);

        Thread thread = new Thread(tailer);
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
    public void init(Tailer tailer)
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

        logger.error("Tailer error: file not found: {}", tailer.getFile().getAbsolutePath());

        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException ignored)
        {
        }
    }

    @Override
    public void fileRotated()
    {
        logger.debug("Tailer: file was rotated: {}", tailer.getFile().getAbsolutePath());

        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException ignored)
        {
        }
    }

    @Override
    public void handle(String line)
    {
        try
        {
            lineProcessor.handle(line);
        }
        catch (Exception e)
        {
            logger.error("Line processing error: " + e.getMessage());
        }
    }

    @Override
    public void handle(Exception ex)
    {
        if (ex instanceof FileNotFoundException && dynamicFilename)
        {
            restart();
            return;
        }

        logger.error("Tailer error: {}", ex.getMessage());

        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException ignored)
        {
        }
    }
}
