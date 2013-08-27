package tv.ustream.yolo.config;

/**
 * @author bandesz
 */
public class ConfigException extends Exception
{

    public ConfigException(String message)
    {
        super("Configuration error: " + message);
    }

}
