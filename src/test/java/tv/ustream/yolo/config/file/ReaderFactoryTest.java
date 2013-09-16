package tv.ustream.yolo.config.file;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author bandesz
 */
public class ReaderFactoryTest
{

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private File testFile;

    @Before
    public void setUp() throws Exception
    {
        testFile = tmpFolder.newFile();

        FileWriter out = new FileWriter(testFile);
        out.write("{}");
        out.close();
    }

    @Test
    public void createShouldThrowExceptionWhenFileDoesNotExist() throws IOException
    {
        thrown.expect(IOException.class);

        ReaderFactory.createReader(new File(tmpFolder.getRoot().getAbsolutePath() + "/nonexistent.x"), 0, null);
    }

    @Test
    public void createShouldReturnFileReaderForFile() throws IOException
    {
        Reader reader = ReaderFactory.createReader(testFile, 0, null);

        Assert.assertEquals(reader.getClass(), FileReader.class);
    }

    @Test
    public void createShouldReturnDirectoryReaderForDir() throws IOException
    {
        Reader reader = ReaderFactory.createReader(tmpFolder.getRoot(), 0, null);

        Assert.assertEquals(reader.getClass(), DirectoryReader.class);
    }

}
