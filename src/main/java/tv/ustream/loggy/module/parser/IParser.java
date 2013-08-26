package tv.ustream.loggy.module.parser;

import tv.ustream.loggy.module.IModule;

import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public interface IParser extends IModule
{

    public Map<String, String> parse(String line);

    public boolean runAlways();

    public List<String> getOutputKeys();

}
