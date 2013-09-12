package tv.ustream.yolo.client;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author bandesz
 */
public class GraphiteClientTest
{

    private GraphiteClient client;

    private Socket socket;

    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() throws Exception
    {
        socket = mock(Socket.class);
        outputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(outputStream);

        client = createClient("host", 1234, 10, "");
    }

    @Test
    public void clientShouldSendDataThroughSocket() throws Exception
    {
        client.sendMetrics("key1", 1234.5);

        await().atMost(500, TimeUnit.MILLISECONDS).until(outputStreamHasData("key1 1234.500000"));
    }

    @Test
    public void shouldUseCustomTimestamp() throws Exception
    {
        client.sendMetrics("key1", 1234.5, 1234567890);

        await().atMost(500, TimeUnit.MILLISECONDS).until(outputStreamHasData("key1 1234.500000 1234567890\n"));
    }

    @Test
    public void shouldUsePrefix() throws Exception
    {
        client = createClient("host", 1234, 10, "prefix");

        client.sendMetrics("key1", 1234.5);

        await().atMost(500, TimeUnit.MILLISECONDS).until(outputStreamHasData("prefix.key1 1234.500000"));
    }

    @Test
    public void shouldSendMultipleData() throws Exception
    {
        client.sendMetrics("key1", 1234.5, 1234567890);
        client.sendMetrics("key2", 1234.6, 1234567891);

        await().atMost(500, TimeUnit.MILLISECONDS).until(
                outputStreamHasData("key1 1234.500000 1234567890\nkey2 1234.600000 1234567891\n")
        );
    }

    @Test
    public void stopShouldStopFlushing() throws Exception
    {
        client.stop();

        Thread.sleep(20);

        client.sendMetrics("key1", 1234.5, 1234567890);

        Thread.sleep(20);

        Assert.assertFalse(outputStream.toString().contains("key1 1234.500000 1234567890\n"));

    }

    private GraphiteClient createClient(final String host, final int port, final long flushTimeMs, final String prefix)
    {
        GraphiteClient newClient = new GraphiteClient(host, port, flushTimeMs, prefix)
        {
            @Override
            protected Socket createSocket(final String host, final int port) throws IOException
            {
                return socket;
            }
        };
        return newClient;
    }

    public Callable<Boolean> outputStreamHasData(final String data)
    {
        return new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                return outputStream.toString().contains(data);
            }
        };
    }

}
