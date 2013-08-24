package com.ustream.loggy.module.parser;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;
import java.util.regex.PatternSyntaxException;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class RegexpParserTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void missingRegexShouldThrowException()
    {
        thrown.expect(IllegalArgumentException.class);

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("params", "value");

        RegexpParser parser = new RegexpParser();
        parser.setUp(config, false);
    }

    @Test
    public void emptyRegexShouldThrowException()
    {
        thrown.expect(IllegalArgumentException.class);

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("regex", "");

        RegexpParser parser = new RegexpParser();
        parser.setUp(config, false);
    }

    @Test
    public void simpleMatchShouldReturnEmptyMap()
    {
        RegexpParser parser = new RegexpParser();
        parser.setUp(createConfig("[a-z]+[0-9]+"), false);

        Map<String, String> actual = parser.parse("___abcd0123____");

        Assert.assertEquals(new HashMap<String, String>(), actual);
    }

    @Test
    public void namedMatchShouldReturnNamedGroups()
    {
        RegexpParser parser = new RegexpParser();
        parser.setUp(createConfig("(?<first>[a-z]+)(?<second>[0-9]+)"), false);

        Map<String, String> actual = parser.parse("___abcd0123___");

        Map<String, String> expected = new HashMap<String, String>();
        expected.put("first", "abcd");
        expected.put("second", "0123");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void namedMatchShouldReturnFirstMatch()
    {
        RegexpParser parser = new RegexpParser();
        parser.setUp(createConfig("(?<first>[a-z]+)(?<second>[0-9]+)"), false);

        Map<String, String> actual = parser.parse("___abcd0123___efgh4567___");

        Map<String, String> expected = new HashMap<String, String>();
        expected.put("first", "abcd");
        expected.put("second", "0123");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void multipleMatchShouldReturnDifferentResults()
    {
        RegexpParser parser = new RegexpParser();
        parser.setUp(createConfig("(?<first>[a-z]+)(?<second>[0-9]+)"), false);

        Map<String, String> actual = parser.parse("___abcd0123___");

        Map<String, String> expected = new HashMap<String, String>();
        expected.put("first", "abcd");
        expected.put("second", "0123");

        Assert.assertEquals(expected, actual);

        Map<String, String> actual2 = parser.parse("___xxxx___");

        Assert.assertNull(actual2);
    }

    @Test
    public void noMatchShouldReturnNull()
    {
        RegexpParser parser = new RegexpParser();
        parser.setUp(createConfig("(?<first>[a-z]+)(?<second>[0-9]+)"), false);

        Map<String, String> actual = parser.parse("___abcd____");

        Assert.assertNull(actual);
    }

    @Test
    public void invalidRegexpShouldThrowException()
    {
        thrown.expect(PatternSyntaxException.class);

        RegexpParser parser = new RegexpParser();
        parser.setUp(createConfig("(abcd"), false);
    }

    @Test
    public void outputParametersCheck()
    {
        RegexpParser parser = new RegexpParser();
        parser.setUp(createConfig("(?<first>[a-z]+)(?<second>[0-9]+)"), false);

        Map<String, String> actual = parser.parse("___abcd0123___");

        Assert.assertTrue(actual.keySet().containsAll(parser.getOutputParameters()));
        Assert.assertTrue(parser.getOutputParameters().containsAll(actual.keySet()));
    }

    private Map<String, Object> createConfig(String regex)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("regex", regex);
        return config;
    }

}
