package tv.ustream.yolo.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TailerListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class GzipTailerTest
{

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private File testFile;

    @Mock
    private TailerListener tailerListener;

    private GzipTailer tailer;

    private GZIPOutputStream gzipOutputStream;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception
    {
        if (tailer != null)
        {
            tailer.stop();
        }
        IOUtils.closeQuietly(gzipOutputStream);
    }

    @Test
    public void shouldReadFileFromBeginning() throws Exception
    {
        String content1 = getLine("l1", 100) + "\n" + getLine("l2", 100) + "\n" + getLine("l3", 100) + "\n";
        testFile = setUpTestFile(null, content1, 0L);

        tailer = GzipTailer.create(testFile, tailerListener, 100, false);

        verify(tailerListener, timeout(1000)).handle(getLine("l1", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l2", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l3", 100));
    }

    @Test
    public void shouldReadFileFromEnd() throws Exception
    {
        String content1 = getLine("l1", 100) + "\n" + getLine("l2", 100) + "\n" + getLine("l3", 100) + "\n";
        testFile = setUpTestFile(null, content1, 0L);

        tailer = GzipTailer.create(testFile, tailerListener, 100, true);

        Thread.sleep(500);

        String content2 = getLine("l4", 100) + "\n" + getLine("l5", 100) + "\n" + getLine("l6", 100) + "\n";
        gzipOutputStream.write(content2.getBytes());
        gzipOutputStream.flush();

        verify(tailerListener, timeout(1000)).handle(getLine("l4", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l5", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l6", 100));

        String content3 = getLine("l7", 100) + "\n" + getLine("l8", 100) + "\n" + getLine("l9", 100) + "\n";
        gzipOutputStream.write(content3.getBytes());
        gzipOutputStream.flush();

        verify(tailerListener, timeout(1000)).handle(getLine("l7", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l8", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l9", 100));
    }

    @Test
    public void shouldReadFileFromBeginningAndHandleNewData() throws Exception
    {
        String content1 = getLine("l1", 100) + "\n" + getLine("l2", 100) + "\n" + getLine("l3", 100) + "\n";

        testFile = setUpTestFile(null, content1, 0L);

        tailer = GzipTailer.create(testFile, tailerListener, 100, false);

        verify(tailerListener, timeout(1000)).handle(getLine("l1", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l2", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l3", 100));

        String content2 = getLine("l4", 100) + "\n" + getLine("l5", 100) + "\n" + getLine("l6", 100) + "\n";
        gzipOutputStream.write(content2.getBytes());
        gzipOutputStream.flush();

        verify(tailerListener, timeout(1000)).handle(getLine("l4", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l5", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l6", 100));
    }

    @Test
    public void shouldResetReader() throws Exception
    {
        String content1 = getLine("l1", 100) + "\n" + getLine("l2", 100) + "\n" + getLine("l3", 100) + "\n";

        testFile = setUpTestFile(null, content1, 0L);

        tailer = GzipTailer.create(testFile, tailerListener, 100, false);

        verify(tailerListener, timeout(1000)).handle(getLine("l1", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l2", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l3", 100));

        gzipOutputStream.finish();
        gzipOutputStream.close();

        testFile.delete();

        verify(tailerListener, timeout(1000)).fileNotFound();

        String content2 = getLine("l4", 100) + "\n" + getLine("l5", 100) + "\n";
        testFile = setUpTestFile(testFile.getName(), content2, 0L);

        verify(tailerListener, timeout(1000)).handle(getLine("l4", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l5", 100));
    }

    @Test
    public void shouldWorkWithCRLF() throws Exception
    {
        String content1 = getLine("l1", 100) + "\r\n" + getLine("l2", 100) + "\r\n" + getLine("l3", 100) + "\r\n";
        testFile = setUpTestFile(null, content1, 0L);

        tailer = GzipTailer.create(testFile, tailerListener, 100, false);

        verify(tailerListener, timeout(1000)).handle(getLine("l1", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l2", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l3", 100));
    }

    @Test
    public void shouldWorkWithCR() throws Exception
    {
        String content1 = getLine("l1", 100) + "\r" + getLine("l2", 100) + "\r" + getLine("l3", 100) + "\rx";
        testFile = setUpTestFile(null, content1, 0L);

        tailer = GzipTailer.create(testFile, tailerListener, 100, false);

        verify(tailerListener, timeout(1000)).handle(getLine("l1", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l2", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l3", 100));
    }

    @Test
    public void shouldHandleNewlyCreatedFile() throws Exception
    {
        tailer = GzipTailer.create(new File(tmpFolder.getRoot().getAbsolutePath() + "/shouldHandleNewlyCreatedFile"), tailerListener, 100, false);

        verify(tailerListener, timeout(1000)).fileNotFound();

        String content1 = getLine("l1", 100) + "\n" + getLine("l2", 100) + "\n" + getLine("l3", 100) + "\n";
        testFile = setUpTestFile("shouldHandleNewlyCreatedFile", content1, 0L);

        verify(tailerListener, timeout(1000)).handle(getLine("l1", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l2", 100));
        verify(tailerListener, timeout(1000)).handle(getLine("l3", 100));
    }

    private File setUpTestFile(String name, String content, long lastModifiedDiffSec) throws IOException
    {
        File file = name != null ? tmpFolder.newFile(name) : tmpFolder.newFile();

        gzipOutputStream = new GZIPOutputStream(new FileOutputStream(file), true);
        gzipOutputStream.write(content.getBytes());
        gzipOutputStream.flush();

        Assert.assertTrue(file.setLastModified(System.currentTimeMillis() + lastModifiedDiffSec * 1000));

        return file;
    }

    private String getLine(final String id, final int length)
    {
        StringBuilder builder = new StringBuilder(id);
        for (int i = id.length(); i < length; i++)
        {
            builder.append("0");
        }
        return builder.toString();
    }

}
