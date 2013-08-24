package com.ustream.loggy.module.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class PassThruParser implements IParser
{

    @Override
    public Map<String, String> parse(String line)
    {
        Map<String, String> result = new HashMap<String, String>();
        result.put("line", line);
        return result;
    }

    @Override
    public boolean runAlways()
    {
        return true;
    }

    @Override
    public List<String> getOutputParameters()
    {
        return Arrays.asList("line");
    }

    @Override
    public void setUp(Map<String, Object> parameters, boolean debug)
    {
    }

}
