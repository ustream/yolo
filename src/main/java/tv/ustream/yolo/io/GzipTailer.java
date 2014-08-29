package tv.ustream.yolo.io;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: gabor-nyerges
 */
public class GzipTailer extends Tailer
{

    private static final Logger LOG = LoggerFactory.getLogger(GzipTailer.class);

    private static final int DEFAULT_DELAY_MILLIS = 1000;

    private static final String RAF_MODE = "r";

    private static final int DEFAULT_BUFSIZE = 65535;

    private RandomAccessFile reader;

    /**
     * The file which will be tailed.
     */
    private final File file;

    /**
     * The amount of time to wait for the file to be updated.
     */
    private final long delayMillis;

    /**
     * Whether to tail from the end or start of file
     */
    private final boolean end;

    /**
     * The listener to notify of events when tailing.
     */
    private final TailerListener listener;

    /**
     * The tailer will run as long as this value is true.
     */
    private volatile boolean run = true;

    /**
     * Gzip input stream
     */
    private GZIPInputStream gzipInputStream;

    /**
     * Stringbuffer for read data
     */
    private StringBuilder sb = new StringBuilder();

    /**
     * Decompress read buffer
     */
    private byte[] inbuf;

    /**
     * Skip number of compressed bytes
     */
    private long skipCompressedBytes = 0;

    /**
     * Creates a Tailer for the given file, starting from the beginning, with the default delay of 1.0s.
     *
     * @param file     The file to follow.
     * @param listener the TailerListener to use.
     */
    public GzipTailer(File file, TailerListener listener)
    {
        this(file, listener, DEFAULT_DELAY_MILLIS);
    }

    /**
     * Creates a Tailer for the given file, starting from the beginning.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     */
    public GzipTailer(File file, TailerListener listener, long delayMillis)
    {
        this(file, listener, delayMillis, false);
    }

    /**
     * Creates a Tailer for the given file, with a delay other than the default 1.0s.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     */
    public GzipTailer(File file, TailerListener listener, long delayMillis, boolean end)
    {
        this(file, listener, delayMillis, end, DEFAULT_BUFSIZE);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param bufSize     Buffer size
     */
    public GzipTailer(File file, TailerListener listener, long delayMillis, boolean end, int bufSize)
    {
        super(file, listener, delayMillis, end, bufSize);

        this.file = file;
        this.delayMillis = delayMillis;
        this.end = end;

        this.inbuf = new byte[bufSize];

        // Save and prepare the listener
        this.listener = listener;
        listener.init(this);
    }

    public GzipTailer(final File file, final TailerListener listener, final long delayMillis, final boolean end,
                      final boolean reOpen)
    {
        this(file, listener, delayMillis, end);
    }

    public GzipTailer(final File file, final TailerListener listener, final long delayMillis, final boolean end,
                      final boolean reOpen, final int bufSize)
    {
        this(file, listener, delayMillis, end, bufSize);
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param bufSize     buffer size.
     * @return The new tailer
     */
    public static GzipTailer create(File file, TailerListener listener, long delayMillis, boolean end, int bufSize)
    {
        GzipTailer tailer = new GzipTailer(file, listener, delayMillis, end, bufSize);
        Thread thread = new Thread(tailer);
        thread.setDaemon(true);
        thread.start();
        return tailer;
    }

    /**
     * Creates and starts a Tailer for the given file with default buffer size.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @return The new tailer
     */
    public static GzipTailer create(File file, TailerListener listener, long delayMillis, boolean end)
    {
        return create(file, listener, delayMillis, end, DEFAULT_BUFSIZE);
    }

    /**
     * Creates and starts a Tailer for the given file, starting at the beginning of the file
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @return The new tailer
     */
    public static GzipTailer create(File file, TailerListener listener, long delayMillis)
    {
        return create(file, listener, delayMillis, false);
    }

    /**
     * Creates and starts a Tailer for the given file, starting at the beginning of the file
     * with the default delay of 1.0s
     *
     * @param file     the file to follow.
     * @param listener the TailerListener to use.
     * @return The new tailer
     */
    public static GzipTailer create(File file, TailerListener listener)
    {
        return create(file, listener, DEFAULT_DELAY_MILLIS, false);
    }

    /**
     * Follows changes in the file, calling the TailerListener's handle method for each new line.
     */
    @Override
    public void run()
    {
        try
        {
            long last = 0; // The last time the file was checked for changes
            long position = 0; // position within the file
            // Open the file
            while (run && reader == null)
            {
                try
                {
                    initReader();
                }
                catch (FileNotFoundException e)
                {
                    listener.fileNotFound();
                }

                if (reader == null)
                {
                    try
                    {
                        Thread.sleep(delayMillis);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
                else
                {
                    // The current position in the file
                    last = System.currentTimeMillis();
                    skipCompressedBytes = end ? file.length() : 0;
                }
            }

            while (run)
            {
                boolean newer = FileUtils.isFileNewer(file, last); // IO-279, must be done first

                // Check the file length to see if it was rotated
                long length = file.length();

                if (length < position)
                {

                    // File was rotated
                    listener.fileRotated();

                    // Reopen the reader after rotation
                    try
                    {
                        initReader();
                        position = 0;
                        continue;
                    }
                    catch (FileNotFoundException e)
                    {
                        // in this case we continue to use the previous reader and position values
                        listener.fileNotFound();
                    }
                }
                else
                {

                    // File was not rotated

                    // See if the file needs to be read again
                    if (length > position)
                    {
                        // The file has more content than it did last time
                        position = readLines();
                        last = System.currentTimeMillis();
                    }
                    else if (newer)
                    {
                        /*
                         * This can happen if the file is truncated or overwritten with the exact same length of
                         * information. In cases like this, the file position needs to be reset
                         */
                        initReader();

                        // Now we can read new lines
                        position = readLines();
                        last = System.currentTimeMillis();
                    }
                }
                try
                {
                    Thread.sleep(delayMillis);
                }
                catch (InterruptedException e)
                {
                }
            }
        }
        catch (Exception e)
        {

            listener.handle(e);
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Allows the tailer to complete its current loop and return.
     */
    public void stop()
    {
        this.run = false;
    }

    private void initReader() throws IOException
    {
        if (gzipInputStream != null)
        {
            IOUtils.closeQuietly(gzipInputStream);
        }
        if (reader != null)
        {
            IOUtils.closeQuietly(reader);
        }
        reader = new RandomAccessFile(file, RAF_MODE);
        gzipInputStream = new GZIPInputStream(new FileInputStream(reader.getFD()), inbuf.length);
        sb.setLength(0);
    }

    /**
     * Read new lines.
     *
     * @return The new position after the lines have been read
     * @throws java.io.IOException if an I/O error occurs.
     */
    private long readLines() throws IOException
    {
        long pos = reader.getFilePointer();
        boolean seenCR = false;
        int byteCount;

        try
        {
            while (run && ((byteCount = gzipInputStream.read(inbuf, 0, inbuf.length)) != -1))
            {
                pos = reader.getFilePointer();
                for (int i = 0; i < byteCount; i++)
                {
                    byte ch = inbuf[i];
                    switch (ch)
                    {
                        case '\n':
                            seenCR = false; // swallow CR before LF
                            if (pos > skipCompressedBytes)
                            {
                                notifyListener();
                            }
                            sb.setLength(0);
                            break;
                        case '\r':
                            if (seenCR)
                            {
                                sb.append('\r');
                            }
                            seenCR = true;
                            break;
                        default:
                            if (seenCR)
                            {
                                seenCR = false; // swallow final CR
                                if (pos > skipCompressedBytes)
                                {
                                    notifyListener();
                                }
                                sb.setLength(0);
                            }
                            sb.append((char) ch); // add character, not its ascii value
                    }
                }
            }
        }
        catch (EOFException e)
        {
            // ignore
        }
        return pos;
    }

    private void notifyListener()
    {
        listener.handle(sb.toString());
    }

}
