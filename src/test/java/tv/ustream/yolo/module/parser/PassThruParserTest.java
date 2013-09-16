package tv.ustream.yolo.module.parser;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bandesz
 */
@SuppressWarnings("unchecked")
public class PassThruParserTest
{

    @Test
    public void shouldPassThroughLine()
    {
        PassThruParser parser = new PassThruParser();
        parser.setUpModule(new HashMap<String, Object>());

        Map<String, String> actual = parser.parse("This is some line");

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("line", "This is some line");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void outputParametersCheck()
    {
        PassThruParser parser = new PassThruParser();
        parser.setUpModule(new HashMap<String, Object>());

        Map<String, String> actual = parser.parse("This is some line");

        Assert.assertEquals(new ArrayList(actual.keySet()), parser.getOutputKeys());
    }

}
