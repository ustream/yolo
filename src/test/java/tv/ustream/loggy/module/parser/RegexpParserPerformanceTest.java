package tv.ustream.loggy.module.parser;

import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

/**
 * @author bandesz
 */
public class RegexpParserPerformanceTest
{

    @Test
    @Ignore
    public void performanceTestWithLotsOfMatches()
    {
        int byteCount = 50000000;

        List<String> lines = generateLines(byteCount);

        RegexpParser parser = new RegexpParser();
        parser.setUpModule(createConfig("(?<first>[a-z])(?<second>[0-9])"));

        Long startTime = System.currentTimeMillis();
        int found = 0;
        for (String line : lines)
        {
            Map<String, String> result = parser.parse(line);
            found += (result != null ? 1 : 0);
        }
        Long elapsed = System.currentTimeMillis() - startTime;

        printStats(lines.size(), byteCount, found, elapsed);
    }

    @Test
    @Ignore
    public void performanceTestWithFewMatches()
    {
        int byteCount = 50000000;

        List<String> lines = generateLines(byteCount);

        RegexpParser parser = new RegexpParser();
        parser.setUpModule(createConfig("(?<first>[a-z]{5})(?<second>[0-9]{5})"));


        Long startTime = System.currentTimeMillis();
        int found = 0;
        for (String line : lines)
        {
            Map<String, String> result = parser.parse(line);
            found += (result != null ? 1 : 0);
        }
        Long elapsed = System.currentTimeMillis() - startTime;

        printStats(lines.size(), byteCount, found, elapsed);
    }

    private void printStats(int lineCount, int byteCount, int found, long elapsed)
    {
        System.out.format("Line count: %d%n", lineCount);
        System.out.format("Byte count: %d%n", byteCount);
        System.out.format("Found matches: %d%n", found);
        System.out.format("Elapsed time: %d ms%n", elapsed);
        System.out.format("Processing speed: %.02f Mb/s%n", byteCount / 1024.0 / 1024.0 / (elapsed / 1000.0));
    }

    private List<String> generateLines(int length)
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

    private Map<String, Object> createConfig(String regex)
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("regex", regex);
        return config;
    }

}
