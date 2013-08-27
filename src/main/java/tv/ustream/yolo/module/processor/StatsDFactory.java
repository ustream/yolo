package tv.ustream.yolo.module.processor;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

/**
 * @author bandesz
 */
public class StatsDFactory
{

    public StatsDClient createClient(String prefix, String host, int port)
    {
        return new NonBlockingStatsDClient(prefix, host, port);
    }

}
