package tv.ustream.yolo.module;

import tv.ustream.yolo.config.ConfigMap;

import java.util.Map;

/**
 * @author bandesz
 */
public interface IModule
{

    void setUpModule(Map<String, Object> parameters);

    ConfigMap getModuleConfig();

    String getModuleDescription();

}
