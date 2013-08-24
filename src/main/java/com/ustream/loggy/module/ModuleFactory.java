package com.ustream.loggy.module;

import java.util.Map;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ModuleFactory
{

    public <T extends IModule> T create(String className, Map<String, Object> config, boolean debug) throws ReflectiveOperationException
    {
        if (null == className || className.isEmpty())
        {
            throw new ReflectiveOperationException("Class name is empty or missing!");
        }
        try
        {
            T instance = (T) Class.forName(className).newInstance();
            instance.setUp(config, debug);
            return instance;
        }
        catch (ReflectiveOperationException e)
        {
            throw new ReflectiveOperationException("Failed to load class: " + className);
        }
    }

}
