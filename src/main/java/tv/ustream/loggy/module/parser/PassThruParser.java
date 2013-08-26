package tv.ustream.loggy.module.parser;

import tv.ustream.loggy.config.ConfigGroup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
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
    public List<String> getOutputKeys()
    {
        return Arrays.asList("line");
    }

    @Override
    public void setUpModule(Map<String, Object> parameters, boolean debug)
    {
    }

    @Override
    public ConfigGroup getModuleConfig()
    {
        return null;
    }

    @Override
    public String getModuleDescription()
    {
        return "forwards all lines to processor, runs always";
    }

}
