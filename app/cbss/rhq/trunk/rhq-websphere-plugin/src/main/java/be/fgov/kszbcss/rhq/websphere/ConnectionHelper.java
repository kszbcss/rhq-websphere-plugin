package be.fgov.kszbcss.rhq.websphere;

import java.util.Properties;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;


import be.fgov.kszbcss.rhq.websphere.connector.SecureAdminClient;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;

public class ConnectionHelper {
    private static final Log log = LogFactory.getLog(ConnectionHelper.class);
    
    public static AdminClient createAdminClient(Configuration config) throws ConnectorException {
        Properties properties = new Properties();
        
        new ConfigurationBasedProcessLocator(config).getAdminClientProperties(properties);
        
        if (log.isDebugEnabled()) {
            log.debug("Creating AdminClient with properties: " + properties);
        }
        
        AdminClient adminClient = AdminClientFactory.createAdminClient(properties);
        try {
            Subject subject = WSSubject.getRunAsSubject();
            if (log.isDebugEnabled()) {
                log.debug("Subject = " + subject);
            }
            if (subject != null) {
                WSSubject.setRunAsSubject(null);
                adminClient = new SecureAdminClient(adminClient, subject);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
        
        return adminClient;
    }
}
