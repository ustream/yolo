package tv.ustream.yolo.module.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.yolo.config.ConfigMap;
import tv.ustream.yolo.config.ConfigValue;
import tv.ustream.yolo.module.parser.scriptengine.IScriptParser;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class ScriptEngineParser implements IParser
{

    private static final Logger LOG = LoggerFactory.getLogger(ScriptEngineParser.class);

    private ScriptEngineManager factory = new ScriptEngineManager();

    private ScriptEngine scriptEngine;

    private String scriptFile;

    private IScriptParser parserImpl;

    @Override
    public Map<String, String> parse(final String line)
    {
        try
        {
            return parserImpl.parse(line);
        }
        catch (Exception e)
        {
            LOG.debug("Script parsing error: {} - {}", e.getClass().getName(), e.getMessage());
            return null;
        }
    }

    @Override
    public boolean runAlways()
    {
        return false;
    }

    @Override
    public List<String> getOutputKeys()
    {
        return null;
    }

    @Override
    public void setUpModule(final Map<String, Object> parameters)
    {
        scriptFile = (String) parameters.get("file");
        String engine = (String) parameters.get("engine");

        scriptEngine = factory.getEngineByName(engine);

        if (!(scriptEngine instanceof Invocable))
        {
            throw new RuntimeException("Script engine is not invocable!");
        }

        try
        {
            scriptEngine.eval(new FileReader(scriptFile));
        }
        catch (ScriptException e)
        {
            throw new RuntimeException("Script file parsing error: " + e.getMessage());
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException("Script file not found: " + scriptFile);
        }

        Object obj = scriptEngine.get("parser");

        if (obj == null)
        {
            throw new RuntimeException("Script does not have an object named parser!");
        }

        parserImpl = ((Invocable) scriptEngine).getInterface(obj, IScriptParser.class);
        if (parserImpl == null)
        {
            throw new RuntimeException("Parser object does not implement the IScriptParser interface!");
        }
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        ConfigMap config = new ConfigMap();
        ConfigValue<String> engine = new ConfigValue<String>(String.class);
        engine.setAllowedValues(Arrays.asList("JavaScript"));
        config.addConfigValue("engine", String.class);
        config.addConfigValue("file", String.class);
        return config;
    }

    @Override
    public String getModuleDescription()
    {
        return "parses data with an external script file";
    }
}
