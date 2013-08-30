package tv.ustream.yolo.module;

import tv.ustream.yolo.config.ConfigMap;

import java.util.Map;

/**
 * @author bandesz
 */
public interface IModule
{

    public void setUpModule(Map<String, Object> parameters);

    public ConfigMap getModuleConfig();

    public String getModuleDescription();

}
