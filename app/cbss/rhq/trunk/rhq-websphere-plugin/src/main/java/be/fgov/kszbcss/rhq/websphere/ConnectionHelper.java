package be.fgov.kszbcss.rhq.websphere;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;

import be.fgov.kszbcss.rhq.websphere.connector.AdminClientProvider;
import be.fgov.kszbcss.rhq.websphere.connector.SecureAdminClientProvider;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.exception.ConnectorException;

public class ConnectionHelper {
    private static final Log log = LogFactory.getLog(ConnectionHelper.class);
    
    // TODO: we probably don't need this code as a separate method; merge it into WebSphereServerDiscoveryComponent
    public static AdminClient createAdminClient(Configuration config) throws ConnectorException {
        final Properties properties = new Properties();
        
        new ConfigurationBasedProcessLocator(config).getAdminClientProperties(properties);
        
        if (log.isDebugEnabled()) {
            log.debug("Creating AdminClient with properties: " + properties);
        }
        
        return new SecureAdminClientProvider(new AdminClientProvider() {
            public AdminClient createAdminClient() throws ConnectorException {
                return AdminClientFactory.createAdminClient(properties);
            }
        }).createAdminClient();
    }
}
