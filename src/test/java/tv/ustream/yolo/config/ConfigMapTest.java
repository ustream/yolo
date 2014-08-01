package tv.ustream.yolo.config;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author bandesz
 */
public class ConfigMapTest
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private IConfigEntry configEntry1;

    private IConfigEntry configEntry2;

    @Before
    public void setUp()
    {
        configEntry1 = mock(IConfigEntry.class);
        configEntry2 = mock(IConfigEntry.class);
    }

    @Test
    public void parseShouldThrowExceptionForNonMapInput() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        new ConfigMap().parse("name", "value");
    }

    @Test
    public void parseShouldParseAllConfigEntriesAndReturnList() throws ConfigException
    {
        ConfigMap map = new ConfigMap();
        map.addConfigEntry("k1", configEntry1);
        map.addConfigEntry("k2", configEntry2);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("k1", "value1");
        data.put("k2", "value2");

        when(configEntry1.parse("name.k1", "value1")).thenReturn("newvalue1");
        when(configEntry2.parse("name.k2", "value2")).thenReturn("newvalue2");

        Map<String, Object> result = map.parse("name", data);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("newvalue1", result.get("k1"));
        Assert.assertEquals("newvalue2", result.get("k2"));
    }

    @Test
    public void parseShouldThrowExceptionWhenEntryIsInvalid() throws ConfigException
    {
        thrown.expect(ConfigException.class);

        ConfigMap map = new ConfigMap();
        map.addConfigEntry("k1", configEntry1);
        map.addConfigEntry("k2", configEntry2);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("k1", "value1");
        data.put("k2", "value2");

        when(configEntry1.parse("name.k1", "value1")).thenReturn("newvalue1");
        when(configEntry2.parse("name.k2", "value2")).thenThrow(new ConfigException("error"));

        map.parse("name", data);
    }

    @Test
    public void mergeShouldMergeTwoConfigMaps() throws ConfigException
    {
        ConfigMap cm1 = new ConfigMap();
        cm1.addConfigEntry("k1", configEntry1);

        ConfigMap cm2 = new ConfigMap();
        cm2.addConfigEntry("k2", configEntry2);

        cm2.merge(cm1);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("k1", "value1");
        data.put("k2", "value2");

        cm2.parse("name", data);

        verify(configEntry1).parse("name.k1", "value1");
        verify(configEntry2).parse("name.k2", "value2");
    }

    @Test
    public void testAddConfigValueMethods() throws ConfigException
    {
        ConfigMap map = new ConfigMap();
        map.addConfigValue("n1", Integer.class);
        map.addConfigValue("n2", Integer.class, false, 5);
        map.addConfigEntry("n3", new ConfigValue<Integer>(Integer.class, false, 10));

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("n1", 1);

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("n1", 1);
        expected.put("n2", 5);
        expected.put("n3", 10);

        Assert.assertEquals(expected, map.parse("root", data));
    }

}
