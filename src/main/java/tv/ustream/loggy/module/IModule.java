package tv.ustream.loggy.module;

import tv.ustream.loggy.config.ConfigGroup;

import java.util.Map;

/**
 * @author bandesz
 */
public interface IModule
{

    public void setUpModule(Map<String, Object> parameters, boolean debug);

    public ConfigGroup getModuleConfig();

    public String getModuleDescription();

}
