package com.ustream.loggy.module;

import com.ustream.loggy.config.ConfigException;
import com.ustream.loggy.config.ConfigGroup;

import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ModuleFactory
{

    @SuppressWarnings("unchecked")
    public <T extends IModule> T create(String name, String className, Map<String, Object> config, boolean debug) throws ConfigException
    {
        try
        {
            T instance = (T) Class.forName(className).newInstance();

            ConfigGroup moduleConfig = instance.getModuleConfig();
            if (moduleConfig != null)
            {
                instance.getModuleConfig().validate(name, config);
            }

            instance.setUp(config, debug);

            return instance;
        }
        catch (ReflectiveOperationException e)
        {
            throw new ConfigException("failed to load class: " + className);
        }
    }

}
