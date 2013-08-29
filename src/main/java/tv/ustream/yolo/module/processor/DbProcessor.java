package tv.ustream.yolo.module.processor;

import tv.ustream.yolo.config.ConfigException;
import tv.ustream.yolo.config.ConfigGroup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author bandesz
 */
public class DbProcessor implements IProcessor
{

    private Connection dbConnection;

    private PreparedStatement stmt;

    private int stmtCounter = 0;

    private int batchSize;

    @Override
    public ConfigGroup getProcessParamsConfig()
    {
        ConfigGroup config = new ConfigGroup();
        config.addConfigValue("query", String.class);
        config.addConfigValue("mapping", List.class);
        return config;
    }

    @Override
    public void validateProcessParams(List<String> parserOutputKeys, Map<String, Object> params) throws ConfigException
    {
    }

    @Override
    public void process(Map<String, String> parserOutput, Map<String, Object> processParams)
    {
        try
        {
            if (stmt == null)
            {
                stmt = dbConnection.prepareStatement((String) processParams.get("query"));
            }
            stmt.setString(1, parserOutput.get("line"));
            stmt.addBatch();

            stmtCounter++;
            if (stmtCounter % batchSize == 0)
            {
                stmt.executeBatch();
                dbConnection.commit();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setUpModule(Map<String, Object> parameters)
    {
        try
        {
            dbConnection = DriverManager.getConnection((String) parameters.get("jdbcUrl"));
            dbConnection.setAutoCommit(false);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to connect to database: " + (String) parameters.get("jdbcUrl"));
        }

        batchSize = ((Number) parameters.get("batchSize")).intValue();
    }

    @Override
    public ConfigGroup getModuleConfig()
    {
        ConfigGroup config = new ConfigGroup();
        config.addConfigValue("jdbcUrl", String.class);
        config.addConfigValue("batchSize", Number.class, false, 1000);
        return config;
    }

    @Override
    public String getModuleDescription()
    {
        return "Inserts the lines to a mysql database";
    }
}
