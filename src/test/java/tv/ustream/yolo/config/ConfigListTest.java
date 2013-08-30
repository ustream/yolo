package tv.ustream.yolo.config;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author bandesz
 */
public class ConfigListTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private IConfigEntry configEntry;

    @Before
    public void setUp()
    {
        configEntry = mock(IConfigEntry.class);
    }

    @Test
    public void parseShouldThrowExceptionWhenListIsInvalid() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        new ConfigList(configEntry).parse("name", new Object());
    }

    @Test
    public void parseShouldParseAllConfigEntriesAndReturnList() throws ConfigException
    {
        ConfigList list = new ConfigList(configEntry);

        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        Map<String, Object> d1 = new HashMap<String, Object>();
        d1.put("k1", "v2");

        Map<String, Object> d2 = new HashMap<String, Object>();
        d2.put("k1", "v3");

        data.add(d1);
        data.add(d2);

        when(configEntry.parse("name[0]", d1)).thenReturn(d1);
        when(configEntry.parse("name[1]", d2)).thenReturn(d2);

        List result = list.parse("name", data);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(d1, data.get(0));
        Assert.assertEquals(d2, data.get(1));
    }

    @Test
    public void parseShouldThrowExceptionWhenEntryIsInvalid() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        ConfigList list = new ConfigList(configEntry);

        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        Map<String, Object> d1 = new HashMap<String, Object>();
        d1.put("k1", "v2");

        Map<String, Object> d2 = new HashMap<String, Object>();
        d2.put("k2", "v3");

        data.add(d1);
        data.add(d2);

        when(configEntry.parse(anyString(), any())).thenThrow(new ConfigException("error"));

        list.parse("name", data);
    }

}
