package com.ustream.loggy.handler;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import java.io.File;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class FileHandler implements TailerListener
{

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

    @Override
    public void init(Tailer tailer)
    {
    }

    @Override
    public void fileNotFound()
    {
        if (debug)
        {
            System.out.println("Tail error: file not found");
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
            System.out.println("Tail: file was rotated");
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
            System.out.println("Tail error: " + ex.getMessage());
        }
    }
}
