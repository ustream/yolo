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
            "\"parsers\":{\"passthru\": {\"class\": \"tv.ustream.yolo.module.parser.PassThruParser\", \"processor\": \"test\"}}," +
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
    public void shouldReadConfigAndReadFileAndWriteToConsole() throws Exception
    {
        yolo = new Yolo();
        String params[] = {"-config", configFile.getAbsolutePath(), "-file", testFile.getAbsolutePath(), "-whole"};
        yolo.start(params);

        Thread.sleep(100);

        Assert.assertTrue(TestProcessor.data.contains("{line=l1}|null"));
        Assert.assertTrue(TestProcessor.data.contains("{line=l2}|null"));
        Assert.assertTrue(TestProcessor.data.contains("{line=l3}|null"));

        FileWriter out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        Thread.sleep(1000);

        Assert.assertTrue(TestProcessor.data.contains("{line=l4}|null"));
        Assert.assertTrue(TestProcessor.data.contains("{line=l5}|null"));
    }

}
