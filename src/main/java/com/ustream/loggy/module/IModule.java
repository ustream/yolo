package com.ustream.loggy.module;

import com.ustream.loggy.config.ConfigGroup;

import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public interface IModule
{

    public void setUpModule(Map<String, Object> parameters, boolean debug);

    public ConfigGroup getModuleConfig();

}
