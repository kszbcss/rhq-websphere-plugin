package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains a local DB2 data source (for monitoring connections) as well as configuration
 * information from the data source configured in WebSphere. When a configuration change is
 * detected, a new instance is created and the old one discarded.
 */
public class ConnectionContext {
    private static final Log log = LogFactory.getLog(ConnectionContext.class);
    
    private final Map<String,Object> dataSourceProperties;
    private final String principal;
    private final String credentials;
    private Connection connection;
    
    public ConnectionContext(Map<String,Object> dataSourceProperties, String principal, String credentials) {
        this.dataSourceProperties = dataSourceProperties;
        this.principal = principal;
        this.credentials = credentials;
    }

    public Map<String,Object> getDataSourceProperties() {
        return dataSourceProperties;
    }
    
    public <T> T execute(Query<T> query) throws SQLException {
        if (connection == null) {
            Properties info = new Properties();
            for (Map.Entry<String,Object> entry : dataSourceProperties.entrySet()) {
                Object value = entry.getValue();
                if (value != null && !value.equals("")) {
                    info.setProperty(entry.getKey(), value.toString());
                }
            }
            info.setProperty("clientProgramName", "RHQ");
            if (principal != null) {
                info.setProperty("user", principal);
                info.setProperty("password", credentials);
            }
            String url = "jdbc:db2://" + info.remove("serverName") + ":" + info.remove("portNumber") + "/" + info.remove("databaseName");
            if (log.isDebugEnabled()) {
                log.debug("Attempting to connect with URL " + url + " and properties " + info);
            }
            connection = DriverManager.getConnection(url, info);
        }
        try {
            return query.execute(connection);
        } catch (SQLException ex) {
            // Discard connection; we will re-attempt next time
            try {
                connection.close();
            } catch (SQLException ex2) {
                // Ignore
            }
            connection = null;
            throw ex;
        }
    }
    
    public void destroy() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                log.error("Error closing database connection", ex);
            }
        }
    }
}
