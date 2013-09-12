package tv.ustream.yolo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author bandesz
 */
public class GraphiteClient
{

    private static final Logger LOG = LoggerFactory.getLogger(GraphiteClient.class);

    private static final int BUFFER_SIZE = 64000;

    private final String host;

    private final int port;

    private String prefix = "";

    private final StringBuffer buffer = new StringBuffer(BUFFER_SIZE);

    private int metricsCount = 0;

    private final Timer flushTimer = new Timer();

    public GraphiteClient(final String host, final int port, final long flushTimeMs, final String prefix)
    {
        this.host = host;
        this.port = port;
        if (prefix != null && !prefix.isEmpty())
        {
            this.prefix = prefix + ".";
        }

        flushTimer.schedule(createTimerTask(), flushTimeMs, flushTimeMs);
    }

    public void sendMetrics(final String key, final Double value)
    {
        sendMetrics(key, value, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
    }

    public void sendMetrics(final String key, final Double value, final long timestamp)
    {
        LOG.debug("graphite: {}{} {} {}", prefix, key, value, timestamp);

        buffer.append(String.format("%s%s %f %d\n", prefix, key, value, timestamp));

        metricsCount++;
    }

    private TimerTask createTimerTask()
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    flush();
                }
                catch (IOException e)
                {
                    LOG.debug("Failed to send graphite data: {}", e.getMessage());
                }
            }
        };
    }

    public void flush() throws IOException
    {
        if (metricsCount == 0)
        {
            return;
        }

        Socket socket = null;
        PrintWriter out = null;
        try
        {
            socket = createSocket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);

            synchronized (buffer)
            {
                LOG.debug("Flushing {} metrics", metricsCount);
                out.write(buffer.toString());
                buffer.delete(0, buffer.length());
                metricsCount = 0;
            }
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
            if (socket != null)
            {
                socket.close();
            }
        }
    }

    public void stop()
    {
        flushTimer.cancel();
    }

    protected Socket createSocket(final String host, final int port) throws IOException
    {
        return new Socket(host, port);
    }

}
