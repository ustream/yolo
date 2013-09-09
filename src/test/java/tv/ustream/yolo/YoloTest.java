package tv.ustream.yolo;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;

/**
 * @author bandesz
 */
public class YoloTest
{

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private File configFile;

    private File testFile;

    private Yolo yolo;

    @Before
    public void setUp() throws Exception
    {
        TestProcessor.data = new ArrayList<String>();

        FileWriter out;

        configFile = tmpFolder.newFile();

        out = new FileWriter(configFile);

        out.write("{" +
            "\"parsers\":{\"passthru\": {\"class\": \"tv.ustream.yolo.module.parser.PassThruParser\", \"processors\": {\"test\": {}}}}," +
            "\"processors\":{\"test\": { \"class\": \"tv.ustream.yolo.TestProcessor\"}}" +
            "}");
        out.close();

        testFile = tmpFolder.newFile();

        out = new FileWriter(testFile);
        out.write("l1\nl2\nl3\n");
        out.close();
    }

    @After
    public void tearDown()
    {
        yolo.stop();
    }

    @Test
    public void shouldReadAndProcessFile() throws Exception
    {
        yolo = new Yolo();
        String params[] = {"-config", configFile.getAbsolutePath(), "-file", testFile.getAbsolutePath(), "-whole"};
        yolo.start(params);

        await().atMost(5000, TimeUnit.MILLISECONDS).until(containsLine("{line=l1}|{}", "{line=l2}|{}", "{line=l3}|{}"));

        FileWriter out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        await().atMost(5000, TimeUnit.MILLISECONDS).until(containsLine("{line=l4}|{}", "{line=l5}|{}"));
    }

    @Test
    public void configShouldBeUpdated() throws Exception
    {
        FileWriter out = new FileWriter(configFile);

        out.write("{" +
            "\"parsers\":{\"passthru\": {\"class\": \"tv.ustream.yolo.module.parser.PassThruParser\", \"processors\": {\"test\":{}}, \"enabled\": false}}," +
            "\"processors\":{\"test\": { \"class\": \"tv.ustream.yolo.TestProcessor\"}}" +
            "}");
        out.close();

        yolo = new Yolo();
        String params[] = {"-config", configFile.getAbsolutePath(), "-file", testFile.getAbsolutePath(), "-whole", "-watchConfigInterval", "1"};
        yolo.start(params);

        Thread.sleep(500);

        // First lines will be read

        out = new FileWriter(configFile);

        out.write("{" +
            "\"parsers\":{\"passthru\": {\"class\": \"tv.ustream.yolo.module.parser.PassThruParser\", \"processors\": {\"test\":{}}}}," +
            "\"processors\":{\"test\": { \"class\": \"tv.ustream.yolo.TestProcessor\"}}" +
            "}");
        out.close();

        Thread.sleep(1500);

        out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        await().atMost(5000, TimeUnit.MILLISECONDS).until(containsLine("{line=l4}|{}", "{line=l5}|{}"));

        Assert.assertFalse(TestProcessor.data.contains("{line=l1}|{}"));
        Assert.assertFalse(TestProcessor.data.contains("{line=l2}|{}"));
        Assert.assertFalse(TestProcessor.data.contains("{line=l3}|{}"));
    }

    public Callable<Boolean> containsLine(final String... lines)
    {
        return new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                for (String line : Arrays.asList(lines))
                {
                    if (!TestProcessor.data.contains(line))
                    {
                        return false;
                    }
                }
                return true;
            }
        };
    }

}
