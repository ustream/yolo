package com.ustream.loggy.module.processor;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class StatsDFactory
{

    public StatsDClient createClient(String prefix, String host, int port)
    {
        return new NonBlockingStatsDClient(prefix, host, port);
    }

}
