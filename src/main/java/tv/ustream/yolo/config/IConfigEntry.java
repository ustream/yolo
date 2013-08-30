package tv.ustream.yolo.config;

/**
 * @author bandesz
 */
public interface IConfigEntry<T>
{

    public T parse(String name, Object data) throws ConfigException;

    public String getDescription(String indent);

}
