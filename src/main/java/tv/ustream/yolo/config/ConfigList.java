package tv.ustream.yolo.config;

import java.util.List;

/**
 * @author bandesz
 */
public class ConfigList implements IConfigEntry<List>
{

    private final IConfigEntry configEntry;

    public ConfigList(IConfigEntry configEntry)
    {
        this.configEntry = configEntry;
    }

    @SuppressWarnings("unchecked")
    public List parse(String name, Object data) throws ConfigException
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

    public String getDescription(String indent)
    {
        return String.format("%sList[%n%s%s%n%s]%n", indent, indent, configEntry.getDescription(""), indent);
    }

}
