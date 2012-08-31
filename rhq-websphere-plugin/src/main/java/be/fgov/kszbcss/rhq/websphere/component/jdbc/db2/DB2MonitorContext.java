package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import be.fgov.kszbcss.rhq.websphere.component.jdbc.db2.pool.ConnectionContext;
import be.fgov.kszbcss.rhq.websphere.component.jdbc.db2.pool.ConnectionContextPool;
import be.fgov.kszbcss.rhq.websphere.component.jdbc.db2.pool.Query;

import com.ibm.db2.jcc.DB2ClientRerouteServerList;

/**
 * Contains a local DB2 data source (for monitoring connections) as well as configuration
 * information from the data source configured in WebSphere. When a configuration change is
 * detected, a new instance is created and the old one discarded.
 */
public class DB2MonitorContext {
    private static final Set<String> dataSourcePropertyKeys = new HashSet<String>(Arrays.asList(
        "serverName", "portNumber", "databaseName", "driverType",
        "clientRerouteAlternateServerName", "clientRerouteAlternatePortNumber"));
    
    private final Map<String,Object> dataSourceProperties;
    private final ConnectionContext connectionContext;
    
    public DB2MonitorContext(Map<String,Object> orgDataSourceProperties, String principal, String credentials) {
        this.dataSourceProperties = orgDataSourceProperties;
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
        // Set the ACR configuration explicitly; we want to fail fast
        dataSourceProperties.put("retryIntervalForClientReroute", Integer.valueOf(3));
        dataSourceProperties.put("maxRetriesForClientReroute", Integer.valueOf(1));
        dataSourceProperties.put("loginTimeout", Integer.valueOf(3));
        connectionContext = ConnectionContextPool.getConnectionContext(dataSourceProperties);
    }

    public Map<String,Object> getDataSourceProperties() {
        return dataSourceProperties;
    }
    
    public void testConnection() throws SQLException {
        connectionContext.testConnection();
    }

    public DB2ClientRerouteServerList getClientRerouteServerList() throws SQLException {
        return connectionContext.getClientRerouteServerList();
    }

    public <T> T execute(Query<T> query) throws SQLException {
        return connectionContext.execute(query);
    }

    public void destroy() {
        connectionContext.release();
    }
}
