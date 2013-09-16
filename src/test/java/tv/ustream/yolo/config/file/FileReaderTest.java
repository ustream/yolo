package tv.ustream.yolo.config.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * @author bandesz
 */
public class FileReaderTest
{

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private File testFile;

    private Reader reader;

    private IConfigFileListener listener;

    private ArgumentCaptor<Map> dataCaptor;

    @Before
    public void setUp() throws Exception
    {
        testFile = tmpFolder.newFile("config1.json");

        FileWriter out = new FileWriter(testFile);
        out.write("{\"key\":\"value\"}");
        out.close();

        testFile.setLastModified(System.currentTimeMillis() - 60000);

        listener = mock(IConfigFileListener.class);

        reader = new FileReader(testFile, 100, listener);

        dataCaptor = ArgumentCaptor.forClass(Map.class);
    }

    @After
    public void tearDown() throws Exception
    {
        reader.stop();
    }

    @Test
    public void createShouldThrowExceptionForNonExistentFile() throws IOException
    {
        thrown.expect(IOException.class);

        new FileReader(new File(tmpFolder.getRoot().getAbsolutePath() + "/nonexistent.x"), 100, listener);
    }

    @Test
    public void readerShouldCallListenerWhenConfigRead() throws Exception
    {
        reader.start();

        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("key", "value");

        verify(listener).configChanged(eq("config1"), dataCaptor.capture(), eq(false));

        Assert.assertEquals(expectedData.get("key"), dataCaptor.getValue().get("key"));
    }

    @Test
    public void readerShouldCallListenerWhenConfigChanged() throws Exception
    {
        reader.start();

        verify(listener, timeout(1000)).configChanged(eq("config1"), anyMap(), eq(false));

        FileWriter out = new FileWriter(testFile);
        out.write("{\"key\":\"value2\"}");
        out.close();

        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("key", "value2");

        verify(listener, timeout(1000)).configChanged(eq("config1"), dataCaptor.capture(), eq(true));

        Assert.assertEquals(expectedData.get("key"), dataCaptor.getValue().get("key"));
    }

    @Test
    public void readerShouldStop() throws Exception
    {
        reader.start();

        reader.stop();

        FileWriter out = new FileWriter(testFile);
        out.write("{\"key\":\"value2\"}");
        out.close();

        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("key", "value2");

        verify(listener, never()).configChanged(eq("config1"), anyMap(), eq(true));
    }

}
