package tv.ustream.loggy.module.parser;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import tv.ustream.loggy.config.ConfigException;
import tv.ustream.loggy.module.ModuleFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class RegexpParserTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void missingRegexShouldThrowException() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        createParser(null);
    }

    @Test
    public void emptyRegexShouldThrowException() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        createParser("");
    }

    @Test
    public void simpleMatchShouldReturnEmptyMap() throws ConfigException
    {
        IParser parser = createParser("[a-z]+[0-9]+");

        Map<String, String> actual = parser.parse("___abcd0123____");

        Assert.assertEquals(new HashMap<String, String>(), actual);
    }

    @Test
    public void namedMatchShouldReturnNamedGroups() throws ConfigException
    {
        IParser parser = createParser("(?<first>[a-z]+)(?<second>[0-9]+)");

        Map<String, String> actual = parser.parse("___abcd0123___");

        Map<String, String> expected = new HashMap<String, String>();
        expected.put("first", "abcd");
        expected.put("second", "0123");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void namedMatchShouldReturnFirstMatch() throws ConfigException
    {
        IParser parser = createParser("(?<first>[a-z]+)(?<second>[0-9]+)");

        Map<String, String> actual = parser.parse("___abcd0123___efgh4567___");

        Map<String, String> expected = new HashMap<String, String>();
        expected.put("first", "abcd");
        expected.put("second", "0123");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void multipleMatchShouldReturnDifferentResults() throws ConfigException
    {
        IParser parser = createParser("(?<first>[a-z]+)(?<second>[0-9]+)");

        Map<String, String> actual = parser.parse("___abcd0123___");

        Map<String, String> expected = new HashMap<String, String>();
        expected.put("first", "abcd");
        expected.put("second", "0123");

        Assert.assertEquals(expected, actual);

        Map<String, String> actual2 = parser.parse("___xxxx___");

        Assert.assertNull(actual2);
    }

    @Test
    public void noMatchShouldReturnNull() throws ConfigException
    {
        IParser parser = createParser("(?<first>[a-z]+)(?<second>[0-9]+)");

        Map<String, String> actual = parser.parse("___abcd____");

        Assert.assertNull(actual);
    }

    @Test
    public void invalidRegexpShouldThrowException() throws ConfigException
    {
        thrown.expect(PatternSyntaxException.class);

        createParser("(abcd");
    }

    @Test
    public void outputParametersCheck() throws ConfigException
    {
        IParser parser = createParser("(?<first>[a-z]+)(?<second>[0-9]+)");

        Map<String, String> actual = parser.parse("___abcd0123___");

        Assert.assertTrue(actual.keySet().containsAll(parser.getOutputParameters()));
        Assert.assertTrue(parser.getOutputParameters().containsAll(actual.keySet()));
    }

    private IParser createParser(String regex) throws ConfigException
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("class", RegexpParser.class.getCanonicalName());
        config.put("regex", regex);
        config.put("processor", "p");
        return new ModuleFactory().createParser("x", config, false);
    }

}
