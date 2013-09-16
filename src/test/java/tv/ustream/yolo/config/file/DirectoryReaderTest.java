package tv.ustream.yolo.config.file;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import tv.ustream.yolo.config.ConfigException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * @author bandesz
 */
public class DirectoryReaderTest
{

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private File testFile1;

    private File testFile2;

    private Reader reader;

    private IConfigFileListener listener;

    private ArgumentCaptor<String> nameCaptor;

    private ArgumentCaptor<Map> dataCaptor;

    private ArgumentCaptor<Boolean> updateCaptor;

    @Before
    public void setUp() throws Exception
    {
        testFile1 = tmpFolder.newFile("config1.json");

        FileWriter out = new FileWriter(testFile1);
        out.write("{\"key1\":\"value1\"}");
        out.close();
        testFile1.setLastModified(System.currentTimeMillis() - 60000);

        testFile2 = tmpFolder.newFile("config2.json");

        out = new FileWriter(testFile2);
        out.write("{\"key2\":\"value2\"}");
        out.close();

        testFile2.setLastModified(System.currentTimeMillis() - 60000);

        listener = mock(IConfigFileListener.class);

        reader = new DirectoryReader(tmpFolder.getRoot(), 100, listener);

        nameCaptor = ArgumentCaptor.forClass(String.class);
        dataCaptor = ArgumentCaptor.forClass(Map.class);
        updateCaptor = ArgumentCaptor.forClass(Boolean.class);
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

        new DirectoryReader(new File(tmpFolder.getRoot().getAbsolutePath() + "/nonexistent"), 100, listener);
    }

    @Test
    public void readerShouldCallListenerWhenConfigsRead() throws Exception
    {
        reader.start();

        Map<String, Object> expectedData1 = new HashMap<String, Object>();
        expectedData1.put("key1", "value1");

        Map<String, Object> expectedData2 = new HashMap<String, Object>();
        expectedData2.put("key2", "value2");

        verify(listener).configChanged(eq("config1"), dataCaptor.capture(), eq(false));

        Assert.assertEquals(expectedData1.get("key1"), dataCaptor.getValue().get("key1"));

        verify(listener).configChanged(eq("config2"), dataCaptor.capture(), eq(false));

        Assert.assertEquals(expectedData2.get("key2"), dataCaptor.getValue().get("key2"));
    }

    @Test
    public void readerShouldCallListenerWhenConfigChanged() throws Exception
    {
        reader.start();

        FileWriter out = new FileWriter(testFile2);
        out.write("{\"key2\":\"value4\"}");
        out.close();

        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("key2", "value4");

        verifyConfigChangedAtIndex("config2", "key2", "value4", true, 3);
    }

    @Test
    public void readerShouldCallListenerWhenConfigCreated() throws Exception
    {
        reader.start();

        File newTestFile = tmpFolder.newFile("config3");

        FileWriter out = new FileWriter(newTestFile);
        out.write("{\"key3\":\"value3\"}");
        out.close();

        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("key3", "value3");

        verifyConfigChangedAtIndex("config3", "key3", "value3", true, 3);
    }

    @Test
    public void readerShouldCallListenerWhenConfigDeleted() throws Exception
    {
        reader.start();

        testFile2.delete();

        verify(listener, timeout(200).times(3)).configChanged(
                nameCaptor.capture(),
                dataCaptor.capture(),
                updateCaptor.capture()
        );

        Assert.assertEquals("config2", nameCaptor.getAllValues().get(2));
        Assert.assertNull(dataCaptor.getAllValues().get(2));
        Assert.assertTrue(updateCaptor.getAllValues().get(2));
    }

    @Test
    public void readerShouldStop() throws Exception
    {
        reader.start();

        reader.stop();

        testFile2.delete();

        verify(listener, timeout(200).times(2)).configChanged(anyString(), anyMap(), anyBoolean());
    }

    private void verifyConfigChangedAtIndex(String name, String key, String value, Boolean update, int times)
            throws ConfigException
    {
        verify(listener, timeout(200).times(times))
                .configChanged(nameCaptor.capture(), dataCaptor.capture(), updateCaptor.capture());

        Assert.assertEquals(name, nameCaptor.getAllValues().get(times - 1));
        Assert.assertEquals(1, dataCaptor.getAllValues().get(times - 1).size());
        Assert.assertEquals(value, dataCaptor.getAllValues().get(times - 1).get(key));
        Assert.assertEquals(update, updateCaptor.getAllValues().get(times - 1));
    }

}
