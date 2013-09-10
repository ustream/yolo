package tv.ustream.yolo.config;

/**
 * @author bandesz
 */
public class ConfigException extends Exception
{

    public ConfigException(final String message)
    {
        super("Configuration error: " + message);
    }

}
