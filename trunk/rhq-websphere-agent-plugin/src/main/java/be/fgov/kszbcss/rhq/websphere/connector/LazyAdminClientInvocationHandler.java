package be.fgov.kszbcss.rhq.websphere.connector;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.management.exception.ConnectorNotAvailableException;

public class LazyAdminClientInvocationHandler implements InvocationHandler {
    private static final Log log = LogFactory.getLog(LazyAdminClientInvocationHandler.class);
    
    private final AdminClientProvider provider;
    private long lastAttempt = -1;
    private AdminClient target;
    
    public LazyAdminClientInvocationHandler(AdminClientProvider provider) {
        this.provider = provider;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        synchronized (this) {
            if (target == null) {
                long timestamp = System.currentTimeMillis();
                if (lastAttempt != -1 && timestamp-lastAttempt < 120000) {
                    throw new ConnectorNotAvailableException("The connector is not available because a recent attempt to create the connector failed");
                } else {
                    log.debug("Attempting to create AdminClient...");
                    try {
                        target = provider.createAdminClient();
                        log.debug("AdminClient successfully created");
                    } catch (ConnectorException ex) {
                        log.debug("AdminClient creation failed with ConnectorException", ex);
                        throw ex;
                    } catch (Throwable ex) {
                        log.debug("AdminClient creation failed with unexpected exception", ex);
                        throw new ConnectorNotAvailableException("An attempt to create the connector failed", ex);
                    } finally {
                        lastAttempt = timestamp;
                    }
                }
            }
        }
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
