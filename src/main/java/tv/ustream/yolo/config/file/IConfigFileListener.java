package tv.ustream.yolo.config.file;

import tv.ustream.yolo.config.ConfigException;

import java.util.Map;

/**
 * @author bandesz
 */
public interface IConfigFileListener
{

    void configChanged(String namespace, Map<String, Object> configData, boolean update) throws ConfigException;

}
