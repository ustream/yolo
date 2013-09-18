package tv.ustream.yolo.module.parser;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author bandesz
 */
public class RegexpParserPerformanceTest
{

    private int byteCount = 5000000;

    private List<String> lines;

    @Before
    public void setUp()
    {
        lines = generateLines(byteCount);
    }

    @Test(timeout = 500)
    public void performanceTestWithLotsOfMatches()
    {
        RegexpParser parser = new RegexpParser();
        parser.setUpModule(createConfig("(?<first>[a-z])(?<second>[0-9])"));

        for (String line : lines)
        {
            parser.parse(line);
        }
    }

    @Test(timeout = 500)
    public void performanceTestWithFewMatches()
    {
        RegexpParser parser = new RegexpParser();
        parser.setUpModule(createConfig("(?<first>[a-z]{5})(?<second>[0-9]{5})"));

        for (String line : lines)
        {
            parser.parse(line);
        }
    }

    private List<String> generateLines(final int length)
    {
        List<String> result = new ArrayList<String>();

        StringBuilder buffer = new StringBuilder();

        Random r = new Random();

        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTVWXYZ01234567890";
        for (int i = 0; i < length; i++)
        {
            buffer.append(alphabet.charAt(r.nextInt(alphabet.length())));
            if (r.nextInt(200) == 0)
            {
                result.add(buffer.toString());
                buffer.delete(0, buffer.length());
            }
        }

        return result;
    }

    private Map<String, Object> createConfig(final String regex)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("regex", regex);
        return config;
    }

}
