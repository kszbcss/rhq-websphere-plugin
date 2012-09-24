package be.fgov.kszbcss.rhq.websphere.connector;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;

public class SecureAdminClientProvider implements AdminClientProvider {
    private static final Log log = LogFactory.getLog(SecureAdminClientProvider.class);
    
    private final AdminClientProvider parent;

    public SecureAdminClientProvider(AdminClientProvider parent) {
        this.parent = parent;
    }

    public AdminClient createAdminClient() throws ConnectorException {
        AdminClient adminClient = parent.createAdminClient();
        try {
            Subject subject = WSSubject.getRunAsSubject();
            if (log.isDebugEnabled()) {
                log.debug("Subject = " + subject);
            }
            if (subject != null) {
                WSSubject.setRunAsSubject(null);
                return new SecureAdminClient(adminClient, subject);
            } else {
                return adminClient;
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
}
