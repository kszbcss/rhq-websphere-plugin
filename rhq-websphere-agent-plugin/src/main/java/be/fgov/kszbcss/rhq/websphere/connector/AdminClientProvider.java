package be.fgov.kszbcss.rhq.websphere.connector;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public interface AdminClientProvider {
    AdminClient createAdminClient() throws ConnectorException;
}
