package be.fgov.kszbcss.rhq.websphere.connector;

import java.lang.reflect.Proxy;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.management.exception.ConnectorNotAvailableException;

/**
 * Adds a wrapper that avoids repetitive requests to a server that is unavailable. The wrapper will
 * reject requests for 2 minutes when a {@link ConnectorNotAvailableException} is received.
 */
public class FailFastAdminClientProvider implements AdminClientProvider {
    private final AdminClientProvider parent;

    public FailFastAdminClientProvider(AdminClientProvider parent) {
        this.parent = parent;
    }

    public AdminClient createAdminClient() throws ConnectorException {
        return (AdminClient)Proxy.newProxyInstance(FailFastAdminClientProvider.class.getClassLoader(),
                new Class<?>[] { AdminClient.class },
                new FailFastInvocationHandler(parent.createAdminClient()));
    }
}
