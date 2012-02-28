package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.db2.jcc.DB2ClientRerouteServerList;
import com.ibm.db2.jcc.DB2SimpleDataSource;

/**
 * Contains a local DB2 data source (for monitoring connections) as well as configuration
 * information from the data source configured in WebSphere. When a configuration change is
 * detected, a new instance is created and the old one discarded.
 */
public class ConnectionContext {
    private static final Log log = LogFactory.getLog(ConnectionContext.class);
    
    private static final Set<String> dataSourcePropertyKeys = new HashSet<String>(Arrays.asList(
        "serverName", "portNumber", "databaseName", "driverType",
        "clientRerouteAlternateServerName", "clientRerouteAlternatePortNumber",
        "retryIntervalForClientReroute", "maxRetriesForClientReroute", "loginTimeout"));
    
    private final Map<String,Object> dataSourceProperties;
    private final DB2SimpleDataSource dataSource;
    private Connection connection;
    private long lastSuccessfulQuery;
    
    public ConnectionContext(Map<String,Object> orgDataSourceProperties, String principal, String credentials) {
        this.dataSourceProperties = orgDataSourceProperties;
        dataSource = new DB2SimpleDataSource();
        Map<String,Object> dataSourceProperties = new HashMap<String,Object>();
        for (Map.Entry<String,Object> entry : orgDataSourceProperties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && dataSourcePropertyKeys.contains(key)) {
                dataSourceProperties.put(entry.getKey(), value);
            }
        }
        dataSourceProperties.put("clientProgramName", "RHQ");
        dataSourceProperties.put("user", principal);
        dataSourceProperties.put("password", credentials);
        if (log.isDebugEnabled()) {
            log.debug("Configuring data source with properties " + dataSourceProperties);
        }
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(DB2SimpleDataSource.class);
        } catch (IntrospectionException ex) {
            throw new Error(ex);
        }
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            String name = descriptor.getName();
            if (dataSourceProperties.containsKey(name)) {
                Object value = dataSourceProperties.get(name);
                if (log.isDebugEnabled()) {
                    log.debug("Setting property " + name + ": propertyType=" + descriptor.getPropertyType().getName() + ", value=" + value + " (class=" + (value == null ? "<N/A>" : value.getClass().getName()) + ")");
                }
                try {
                    descriptor.getWriteMethod().invoke(dataSource, value);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException("Failed to set '" + name + "' property", ex);
                } catch (IllegalAccessException ex) {
                    throw new IllegalAccessError(ex.getMessage());
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof RuntimeException) {
                        throw (RuntimeException)cause;
                    } else if (cause instanceof Error) {
                        throw (Error)cause;
                    } else {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

    public Map<String,Object> getDataSourceProperties() {
        return dataSourceProperties;
    }
    
    public void testConnection() throws SQLException {
        execute(new Query<Void>() {
            public Void execute(Connection connection) throws SQLException {
                Statement statement = connection.createStatement();
                try {
                    statement.execute("SELECT 1 FROM SYSIBM.SYSDUMMY1");
                } finally {
                    statement.close();
                }
                return null;
            }
        });
    }
    
    public DB2ClientRerouteServerList getClientRerouteServerList() throws SQLException {
        // Need to make sure that a query has been executed recently so that the reroute information is up to date
        if (System.currentTimeMillis() - lastSuccessfulQuery > 60000) {
            testConnection();
        }
        return dataSource.getClientRerouteServerList();
    }
    
    public <T> T execute(Query<T> query) throws SQLException {
        if (connection == null) {
            connection = dataSource.getConnection();
        }
        try {
            T result = query.execute(connection);
            lastSuccessfulQuery = System.currentTimeMillis();
            return result;
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
