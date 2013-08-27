package tv.ustream.loggy.handler;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author bandesz
 */
public class FileHandler implements TailerListener
{

    private Logger logger = LoggerFactory.getLogger(FileHandler.class);

    private Boolean debug;

    private final Tailer tailer;

    private final ILineHandler lineProcessor;

    public FileHandler(ILineHandler lineProcessor, String filePath, Boolean readWhole, Boolean reopen, Boolean debug)
    {
        this.lineProcessor = lineProcessor;
        this.debug = debug;

        tailer = new Tailer(new File(filePath), this, 1000, !readWhole, reopen);
    }

    public void start()
    {
        Thread thread = new Thread(tailer);
        thread.start();
    }

    public void stop()
    {
        tailer.stop();
    }

    @Override
    public void init(Tailer tailer)
    {
    }

    @Override
    public void fileNotFound()
    {
        if (debug)
        {
            logger.error("Tailer error: file not found: {}", tailer.getFile().getAbsolutePath());
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException ignored)
            {
            }
        }
    }

    @Override
    public void fileRotated()
    {
        if (debug)
        {
            logger.info("Tailer: file was rotated: {}", tailer.getFile().getAbsolutePath());
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException ignored)
            {
            }
        }
    }

    @Override
    public void handle(String line)
    {
        lineProcessor.handle(line);
    }

    @Override
    public void handle(Exception ex)
    {
        if (debug)
        {
            logger.error("Tailer error: {}", ex.getMessage());
        }
    }
}
