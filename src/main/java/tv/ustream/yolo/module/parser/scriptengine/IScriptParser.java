package tv.ustream.yolo.module.parser.scriptengine;

import java.util.Map;

/**
 * @author bandesz
 */
public interface IScriptParser
{

    Map<String, Object> parse(String line);
}
