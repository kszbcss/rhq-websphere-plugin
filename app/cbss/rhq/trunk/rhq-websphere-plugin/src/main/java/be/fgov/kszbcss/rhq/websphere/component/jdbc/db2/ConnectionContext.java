package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private final DataSource dataSource;
    private Connection connection;
    
    public ConnectionContext(Map<String,Object> orgDataSourceProperties, String principal, String credentials) {
        this.dataSourceProperties = orgDataSourceProperties;
        Class<?> dataSourceClass;
        try {
            dataSourceClass = Class.forName(Constants.DATASOURCE_CLASS_NAME);
        } catch (ClassNotFoundException ex) {
            throw new NoClassDefFoundError(ex.getMessage());
        }
        try {
            dataSource = (DataSource)dataSourceClass.newInstance();
        } catch (InstantiationException ex) {
            throw new InstantiationError(ex.getMessage());
        } catch (IllegalAccessException ex) {
            throw new IllegalAccessError(ex.getMessage());
        }
        Map<String,Object> dataSourceProperties = new HashMap<String,Object>();
        for (Map.Entry<String,Object> entry : orgDataSourceProperties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && dataSourcePropertyKeys.contains(key)) {
                dataSourceProperties.put(entry.getKey(), value);
            }
        }
        dataSourceProperties.put("clientProgramName", "RHQ");
        if (principal != null) {
            dataSourceProperties.put("user", principal);
            dataSourceProperties.put("password", credentials);
        }
        if (log.isDebugEnabled()) {
            log.debug("Configuring data source with properties " + dataSourceProperties);
        }
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(dataSourceClass);
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
    
    public <T> T execute(Query<T> query) throws SQLException {
        if (connection == null) {
            connection = dataSource.getConnection();
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
