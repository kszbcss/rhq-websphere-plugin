package be.fgov.kszbcss.rhq.websphere.connector;

import java.lang.reflect.Proxy;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorNotAvailableException;

public final class AdminClientUtils {
    private AdminClientUtils() {}
    
    /**
     * Adds a wrapper that avoids repetitive connections to a server that is unavailable. The
     * wrapper will reject requests for 2 minutes when a {@link ConnectorNotAvailableException} is
     * received.
     * 
     * @param adminClient
     * @return
     */
    public static AdminClient createFailFastAdminClient(AdminClientProvider provider) {
        return (AdminClient)Proxy.newProxyInstance(AdminClientUtils.class.getClassLoader(),
                new Class<?>[] { AdminClient.class }, new FailFastInvocationHandler(provider));
    }
}
