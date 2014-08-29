package tv.ustream.yolo.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;

/**
 * @author bandesz
 */
public class FileHandlerTest
{

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private FileHandler handler;

    private TestLineHandler testLineHandler;

    private File testFile;

    @Before
    public void setUp() throws Exception
    {
        testLineHandler = new TestLineHandler();
    }

    @After
    public void tearDown()
    {
        handler.stop();
    }

    @Test
    public void shouldTailExistingFile() throws Exception
    {
        testFile = setUpTestFile(null, "l1\nl2\nl3\n", 0);

        setupFileHandler(testFile.getName(), false);

        Thread.sleep(100);

        FileWriter out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l4\nl5\n"));
    }

    @Test
    public void shouldTailNewFile() throws Exception
    {
        String filename = "shouldTailNewFile.test";

        setupFileHandler(filename, false);

        Thread.sleep(100);

        setUpTestFile(filename, "l1\nl2\nl3\n", 0);

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\n"));
    }

    @Test
    public void shouldTailMultipleFiles() throws Exception
    {
        String filename1 = "shouldTailMultipleFiles1.test";
        String filename2 = "shouldTailMultipleFiles2.test";

        setupFileHandler("*", false);

        Thread.sleep(100);

        File testFile1 = setUpTestFile(filename1, "l1\nl2\n", 0);
        File testFile2 = setUpTestFile(filename2, "l3\nl4\n", 0);

        Thread.sleep(100);

        FileWriter out = new FileWriter(testFile1, true);
        out.write("l5\n");
        out.close();

        out = new FileWriter(testFile2, true);
        out.write("l6\n");
        out.close();

        await().atMost(5000, TimeUnit.MILLISECONDS).until(containsHandledLines("l1\n", "l2\n", "l3\n", "l4\n", "l5\n", "l6\n"));
    }

    @Test
    public void shouldTailFileFromTheBeginning() throws Exception
    {
        testFile = setUpTestFile(null, "l1\nl2\nl3\n", 0);

        setupFileHandler(testFile.getName(), true);

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\n"));

        FileWriter out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\nl4\nl5\n"));
    }

    @Test
    public void shouldHandleRotate() throws Exception
    {
        testFile = setUpTestFile(null, "l1\nl2\nl3\n", 0);

        setupFileHandler(testFile.getName(), true);

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\n"));

        testFile.delete();

        FileWriter out = new FileWriter(testFile, true);
        out.write("l4\nl5\n");
        out.close();

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\nl4\nl5\n"));
    }

    @Test
    public void shouldHandleFileNameChange() throws Exception
    {
        testFile = setUpTestFile(null, "l1\nl2\nl3\n", 0);

        setupFileHandler("*", true);

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\n"));

        testFile.delete();

        setUpTestFile(null, "l4\nl5\n", 0);

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\nl4\nl5\n"));
    }

    @Test
    public void newerLastModifiedButSameLengthShouldNotResetTailer() throws Exception
    {
        testFile = setUpTestFile(null, "l1\nl2\nl3\n", -10);

        setupFileHandler(testFile.getName(), true);

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\n"));

        Thread.sleep(1000);

        testFile.setLastModified(System.currentTimeMillis());

        Thread.sleep(1000);

        Assert.assertEquals("l1\nl2\nl3\n", testLineHandler.handledLines);
    }

    @Test
    public void shouldHandleProcessingError() throws Exception
    {
        testFile = setUpTestFile(null, "l1\nl2\nl3\n", -10);

        testLineHandler = spy(testLineHandler);
        doCallRealMethod().doThrow(new RuntimeException("x")).doCallRealMethod().when(testLineHandler).handle(anyString());

        setupFileHandler(testFile.getName(), true);

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl3\n"));
    }

    @Test
    public void shouldReadGzipFile() throws Exception
    {
        String filename = "shouldTailNewFile.test";

        setupGzipFileHandler(filename, false);

        Thread.sleep(100);

        setUpGzipTestFile(filename, "l1\nl2\nl3\n", 0);

        await().atMost(5000, TimeUnit.MILLISECONDS).until(equalsHandledLines("l1\nl2\nl3\n"));
    }

    public Callable<Boolean> equalsHandledLines(final String lines)
    {
        return new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                return lines.equals(testLineHandler.handledLines);
            }
        };
    }

    public Callable<Boolean> containsHandledLines(final String... lines)
    {
        return new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                for (String line : lines) {
                    if (!testLineHandler.handledLines.contains(line)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private File setUpTestFile(String name, String content, long lastModifiedDiffSec) throws IOException
    {
        File file = name != null ? tmpFolder.newFile(name) : tmpFolder.newFile();
        try (FileWriter out = new FileWriter(file))
        {
            out.write(content);
        }
        Assert.assertTrue(file.setLastModified(System.currentTimeMillis() + lastModifiedDiffSec * 1000));
        return file;
    }

    private void setupFileHandler(final String filename, final boolean readWhole)
    {
        handler = new FileHandler(
            testLineHandler, tmpFolder.getRoot().getAbsolutePath() + "/" + filename, 100, readWhole, false, false
        );
        handler.start();
    }

    private void setupGzipFileHandler(final String filename, final boolean readWhole)
    {
        handler = new FileHandler(
            testLineHandler, tmpFolder.getRoot().getAbsolutePath() + "/" + filename, 100, readWhole, false, true
        );
        handler.start();
    }

    private File setUpGzipTestFile(String name, String content, long lastModifiedDiffSec) throws IOException
    {
        File file = name != null ? tmpFolder.newFile(name) : tmpFolder.newFile();

        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(file), true);
        gzipOutputStream.write(content.getBytes());
        gzipOutputStream.flush();

        Assert.assertTrue(file.setLastModified(System.currentTimeMillis() + lastModifiedDiffSec * 1000));

        return file;
    }

    private class TestLineHandler implements ILineHandler
    {
        public String handledLines = "";

        @Override
        public void handle(final String line)
        {
            handledLines += line + "\n";
        }
    }

}
