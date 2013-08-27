package tv.ustream.yolo.module;

import tv.ustream.yolo.config.ConfigGroup;

import java.util.Map;

/**
 * @author bandesz
 */
public interface IModule
{

    public void setUpModule(Map<String, Object> parameters);

    public ConfigGroup getModuleConfig();

    public String getModuleDescription();

}
