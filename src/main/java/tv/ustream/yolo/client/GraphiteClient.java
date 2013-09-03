package tv.ustream.yolo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author bandesz
 */
public class GraphiteClient
{

    private static final Logger logger = LoggerFactory.getLogger(GraphiteClient.class);

    private final static int BUFFER_SIZE = 64000;

    private final String host;

    private final int port;

    private final StringBuffer buffer = new StringBuffer(BUFFER_SIZE);

    private int metricsCount = 0;

    public GraphiteClient(String host, int port, long flushTimeMs)
    {
        this.host = host;
        this.port = port;
        new Timer().schedule(createTimerTask(), flushTimeMs, flushTimeMs);
    }

    public void sendMetrics(String key, Double value)
    {
        sendMetrics(key, value, System.currentTimeMillis() / 1000);
    }

    public void sendMetrics(String key, Double value, long timestamp)
    {
        logger.debug("graphite: {} {} {}", key, value, timestamp);

        buffer.append(String.format("%s %f %d\n", key, value, timestamp));

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
                    logger.debug("Failed to send graphite data: {}", e.getMessage());
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
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);

            synchronized (buffer)
            {
                logger.debug("Flushing {} metrics", metricsCount);
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

}
