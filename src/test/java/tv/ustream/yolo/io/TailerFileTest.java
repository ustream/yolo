package tv.ustream.yolo.io;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;

/**
 * @author bandesz
 */
public class TailerFileTest
{
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private File testFile;

    private long originalLastModified;

    @Before
    public void setUp() throws Exception
    {
        testFile = tmpFolder.newFile();
        FileWriter out = new FileWriter(testFile);
        out.write(String.format("content"));
        out.close();
        testFile.setLastModified(System.currentTimeMillis() - 10000);
        originalLastModified = testFile.lastModified();
    }

    @Test
    public void lastModifiedShouldReturnLastModified()
    {
        TailerFile tailerFile = new TailerFile(testFile.getAbsolutePath());
        Assert.assertEquals(originalLastModified, tailerFile.lastModified());
    }

    @Test
    public void lastModifiedShouldReturnNewLastModifiedWhenFileChanged() throws Exception
    {
        TailerFile tailerFile = new TailerFile(testFile.getAbsolutePath());

        FileWriter out = new FileWriter(testFile);
        out.write(String.format("content2"));
        out.close();

        Assert.assertEquals(testFile.lastModified(), tailerFile.lastModified());
    }

    @Test
    public void lastModifiedShouldReturnSameWhenFileSizeNotChanged() throws Exception
    {
        TailerFile tailerFile = new TailerFile(testFile.getAbsolutePath());

        testFile.setLastModified(System.currentTimeMillis());

        Assert.assertEquals(originalLastModified, tailerFile.lastModified());
    }

    @Test
    public void createShouldHandleSameFile()
    {
        TailerFile tailerFile = TailerFile.create(testFile);

        Assert.assertEquals(testFile.getAbsolutePath(), tailerFile.getAbsolutePath());
    }

}
