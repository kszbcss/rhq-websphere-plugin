package be.fgov.kszbcss.rhq.websphere.connector;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorNotAvailableException;

public class FailFastInvocationHandler implements InvocationHandler {
    private static final Log log = LogFactory.getLog(FailFastInvocationHandler.class);
    
    private final AdminClient target;
    private long lastConnectorNotAvailableException = -1;
    
    public FailFastInvocationHandler(AdminClient target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (lastConnectorNotAvailableException != -1 && System.currentTimeMillis()-lastConnectorNotAvailableException > 120000) {
            log.debug("Resetting lastConnectorNotAvailableException");
            lastConnectorNotAvailableException = -1;
        }
        if (lastConnectorNotAvailableException == -1) {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException invocationTargetException) {
                Throwable exception = invocationTargetException.getCause();
                Throwable t = exception;
                do {
                    if (log.isDebugEnabled()) {
                        log.debug("cause = " + t.getClass().getName());
                    }
                    if (t instanceof ConnectorNotAvailableException) {
                        lastConnectorNotAvailableException = System.currentTimeMillis();
                        break;
                    }
                    t = t.getCause();
                } while (t != null);
                if (log.isDebugEnabled()) {
                    if (lastConnectorNotAvailableException == -1) {
                        log.debug("Not setting lastConnectorNotAvailableException");
                    } else {
                        log.debug("Setting lastConnectorNotAvailableException");
                    }
                }
                throw exception;
            }
        } else {
            throw new ConnectorNotAvailableException("The connector has been temporarily marked as unavailable because of a previous ConnectorNotAvailableException");
        }
    }
}
