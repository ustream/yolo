package tv.ustream.yolo.config;

import java.util.List;

/**
 * @author bandesz
 */
public class ConfigList implements IConfigEntry<List>
{

    private final IConfigEntry configEntry;

    public ConfigList(final IConfigEntry configEntry)
    {
        this.configEntry = configEntry;
    }

    @SuppressWarnings("unchecked")
    public List parse(final String name, final Object data) throws ConfigException
    {
        if (!(data instanceof List))
        {
            throw new ConfigException(name + " should be a list");
        }
        List<Object> list = (List) data;
        for (int i = 0; i < list.size(); i++)
        {
            list.set(i, configEntry.parse(name + "[" + i + "]", list.get(i)));
        }
        return list;
    }

    public String getDescription(final String indent)
    {
        return String.format("List [%n%s%s%s]%n", indent + "  ", configEntry.getDescription(indent + "  "), indent);
    }

}
