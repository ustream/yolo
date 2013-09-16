package tv.ustream.yolo.module.parser;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import tv.ustream.yolo.module.ModuleFactory;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bandesz
 */
public class ScriptEngineParserTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void shouldParseLineAndReturnMap() throws Exception
    {
        String script =
                "var parser = new Packages.tv.ustream.yolo.module.parser.scriptengine.IScriptParser {\n" +
                        "    parse: function(line) {\n" +
                        "        return {\"line\": line};\n" +
                        "    }\n" +
                        "}\n";

        File scriptFile = createScriptFile(script);
        IParser parser = createParser(scriptFile.getAbsolutePath(), "JavaScript");

        Map<String, String> actual = parser.parse("line1");

        Assert.assertEquals("line1", actual.get("line"));
        Assert.assertEquals(1, actual.size());
    }

    @Test
    public void shouldReturnNullWhenReturnValueIsInvalid() throws Exception
    {
        String script = "var parser = new Packages.tv.ustream.yolo.module.parser.scriptengine.IScriptParser {\n" +
                "    parse: function(line) {\n" +
                "        return \"not map\";\n" +
                "    }\n" +
                "}\n";

        File scriptFile = createScriptFile(script);
        IParser parser = createParser(scriptFile.getAbsolutePath(), "JavaScript");

        Assert.assertNull(parser.parse("line1"));
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenFileDoesNotExist() throws Exception
    {
        thrown.expect(RuntimeException.class);

        createParser(tmpFolder.getRoot().getAbsolutePath() + "/nonexisting.js", "JavaScript");
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenScriptIsInvalid() throws Exception
    {
        thrown.expect(RuntimeException.class);

        File scriptFile = createScriptFile("function (");
        createParser(scriptFile.getAbsolutePath(), "JavaScript");
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenParserObjectIsMissing() throws Exception
    {
        thrown.expect(RuntimeException.class);

        File scriptFile = createScriptFile("var a = 1;");
        createParser(scriptFile.getAbsolutePath(), "JavaScript");
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenParserIsNotImplementingInterface() throws Exception
    {
        thrown.expect(RuntimeException.class);

        File scriptFile = createScriptFile("var parser = 1;");
        createParser(scriptFile.getAbsolutePath(), "JavaScript");
    }

    private File createScriptFile(final String script) throws Exception
    {
        File scriptFile = tmpFolder.newFile();
        FileWriter out = new FileWriter(scriptFile);
        out.write(script);
        out.close();

        return scriptFile;
    }

    private IParser createParser(final String file, final String engine) throws Exception
    {
        Map<String, Object> processors = new HashMap<String, Object>();
        processors.put("processor1", new HashMap<String, Object>());

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", ScriptEngineParser.class.getCanonicalName());
        config.put("file", file);
        config.put("engine", engine);
        config.put("processors", processors);
        return new ModuleFactory().createParser("x", config);
    }
}
