package tv.ustream.yolo.module;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author bandesz
 */
public class ModuleStore<T extends IModule>
{

    private final Map<String, T> modules = new HashMap<String, T>();

    public void add(final String namespace, final String name, final T module)
    {
        modules.put(namespace + "." + name, module);
    }

    public T get(final String name)
    {
        return modules.get(name);
    }

    public T get(final String name, final String optionalNamespace)
    {
        return modules.get(getName(name, optionalNamespace));
    }

    public boolean contains(final String name, final String optionalNamespace)
    {
        return modules.containsKey(getName(name, optionalNamespace));
    }

    private String getName(final String name, final String optionalNamespace)
    {
        if (name.contains("."))
        {
            return name;
        }
        else
        {
            return optionalNamespace + "." + name;
        }
    }

    public void remove(final String name)
    {
        modules.remove(name);
    }

    public Set<Map.Entry<String, T>> entrySet()
    {
        return modules.entrySet();
    }

    public Collection<T> values()
    {
        return modules.values();
    }

    public Set<Map.Entry<String, T>> namespaceEntrySet(final String namespace)
    {
        Set<Map.Entry<String, T>> filtered = new HashSet<Map.Entry<String, T>>();

        for (Map.Entry<String, T> moduleEntry : modules.entrySet())
        {
            if (moduleEntry.getKey().startsWith(namespace + "."))
            {
                filtered.add(moduleEntry);
            }
        }

        return filtered;
    }

}
