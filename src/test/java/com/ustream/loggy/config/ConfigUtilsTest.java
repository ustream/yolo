package com.ustream.loggy.config;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ConfigUtilsTest
{

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void getConfigFromFileShouldReturnMap() throws Exception
    {
        File file = folder.newFile("sampleconfig.json");
        PrintWriter writer = new PrintWriter(file);
        writer.println("{\"key\":\"value\"}");
        writer.close();

        Map<String, Object> actual = ConfigUtils.getConfigFromFile(file.getAbsolutePath());

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("key", "value");

        Assert.assertEquals(expected, actual);
    }

}
