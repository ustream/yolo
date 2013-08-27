package tv.ustream.loggy.handler;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;

/**
 * @author bandesz
 */
public class FileHandlerTest implements ILineHandler
{

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private FileHandler handler;

    private File testFile;

    private String handledLines;

    @Before
    public void setUp() throws Exception
    {
        handledLines = "";
        testFile = tmpFolder.newFile();
        FileWriter out = new FileWriter(testFile);
        out.write("l1\nl2\nl3\n");
        out.close();
    }

    @After
    public void tearDown()
    {
        handler.stop();
    }

    @Test
    public void shouldTailFile() throws Exception
    {
        handler = new FileHandler(this, testFile.getAbsolutePath(), false, false);
        handler.start();

        Thread.sleep(100);

        FileWriter out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        Thread.sleep(1000);

        Assert.assertEquals("l4\nl5\n", handledLines);
    }

    @Test
    public void shouldTailFileFromTheBeginning() throws Exception
    {
        handler = new FileHandler(this, testFile.getAbsolutePath(), true, false);
        handler.start();

        Thread.sleep(100);

        FileWriter out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        Thread.sleep(1000);

        Assert.assertEquals("l1\nl2\nl3\nl4\nl5\n", handledLines);
    }

    @Test
    public void shouldHandleRotate() throws Exception
    {
        handler = new FileHandler(this, testFile.getAbsolutePath(), true, false);
        handler.start();

        Thread.sleep(100);

        FileWriter out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        Thread.sleep(1000);

        testFile.delete();

        Thread.sleep(1000);

        out = new FileWriter(testFile, true);
        out.write("l6\nl7\n");
        out.close();

        Thread.sleep(1000);

        Assert.assertEquals("l1\nl2\nl3\nl4\nl5\nl6\nl7\n", handledLines);
    }

    @Override
    public void handle(String line)
    {
        handledLines += line + "\n";
    }

}
