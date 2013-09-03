package tv.ustream.yolo.module.processor;

import tv.ustream.yolo.client.GraphiteClient;

/**
 * @author bandesz
 */
public class GraphiteFactory
{

    public GraphiteClient createClient(String host, int port, long flushTimeMs)
    {
        return new GraphiteClient(host, port, flushTimeMs);
    }


}
