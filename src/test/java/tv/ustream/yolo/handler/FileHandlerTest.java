package tv.ustream.yolo.handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;

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
        handler = new FileHandler(this, testFile.getAbsolutePath(), 100, false, false);
        handler.start();

        Thread.sleep(100);

        FileWriter out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        Thread.sleep(200);

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l4\nl5\n"));
    }

    @Test
    public void shouldTailFileFromTheBeginning() throws Exception
    {
        handler = new FileHandler(this, testFile.getAbsolutePath(), 100, true, false);
        handler.start();

        Thread.sleep(100);

        FileWriter out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\nl4\nl5\n"));
    }

    @Test
    public void shouldHandleRotate() throws Exception
    {
        handler = new FileHandler(this, testFile.getAbsolutePath(), 100, true, false);
        handler.start();

        Thread.sleep(200);

        testFile.delete();

        FileWriter out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\nl4\nl5\n"));
    }

    @Test
    public void shouldHandleFileNameChange() throws Exception
    {
        handler = new FileHandler(this, testFile.getAbsolutePath() + "*", 100, true, false);
        handler.start();

        Thread.sleep(200);

        testFile.delete();

        File testFile2 = tmpFolder.newFile(testFile.getName() + "X");
        FileWriter out = new FileWriter(testFile2);
        out.write("l4\nl5\n");
        out.close();

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\nl4\nl5\n"));
    }

    @Override
    public void handle(final String line)
    {
        handledLines += line + "\n";
    }

    public Callable<Boolean> equalsHandledLines(final String lines)
    {
        return new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                return lines.equals(handledLines);
            }
        };
    }

}
