package tv.ustream.loggy.config;

/**
 * @author bandesz <bandesz@ustream.tv>
 */
public class ConfigException extends Exception
{

    public ConfigException(String message)
    {
        super("Configuration error: " + message);
    }

}
