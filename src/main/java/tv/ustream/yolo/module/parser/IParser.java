package tv.ustream.yolo.module.parser;

import tv.ustream.yolo.module.IModule;

import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public interface IParser extends IModule
{

    Map<String, Object> parse(final String line);

    boolean runAlways();

    List<String> getOutputKeys();

}
