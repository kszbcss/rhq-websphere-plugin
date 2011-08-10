package be.fgov.kszbcss.rhq.websphere.mbean;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * An MBean identified by an object name pattern. An instance of this class lazily resolves
 * (completes) the object name and also handles the case where the object name changes (e.g. after a
 * server upgrade).
 */
public class MBeanClient {
    private static final Log log = LogFactory.getLog(MBeanClient.class);
    
    private interface Action<T> {
        public T execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException;
    }
    
    private final ProcessInfo processInfo;
    private final MBeanLocator locator;
    private final Map<Class<?>,Object> proxies = new HashMap<Class<?>,Object>();
    private ObjectName cachedObjectName;
    
    MBeanClient(ProcessInfo processInfo, MBeanLocator locator) {
        this.processInfo = processInfo;
        this.locator = locator;
    }
    
    public MBeanLocator getLocator() {
        return locator;
    }

    public ObjectName getObjectName(boolean refresh) throws JMException, ConnectorException {
        if (refresh) {
            ObjectName objectName = internalGetObjectName();
            synchronized (this) {
                cachedObjectName = objectName;
            }
            return objectName;
        } else {
            synchronized (this) {
                if (cachedObjectName == null) {
                    cachedObjectName = internalGetObjectName();
                }
                return cachedObjectName;
            }
        }
    }
    
    private ObjectName internalGetObjectName() throws JMException, ConnectorException {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to resolve " + locator);
        }
        Set<ObjectName> objectNames = locator.queryNames(processInfo, processInfo.getAdminClient());
        if (log.isDebugEnabled()) {
            log.debug("Result: " + objectNames);
        }
        int size = objectNames.size();
        if (size == 1) {
            return objectNames.iterator().next();
        } else {
            throw new InstanceNotFoundException((size == 0 ? "No MBean" : "Mutiple MBeans") + " found for locator " + locator
                    + " (process=" + processInfo.getProcess() + ", node=" + processInfo.getNode() + ", cell=" + processInfo.getCell() + ")");
        }
    }
    
    public <T> T getProxy(Class<T> iface) {
        synchronized (proxies) {
            Object proxy = proxies.get(iface);
            if (proxy == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating dynamic proxy for MBean " + locator);
                }
                proxy = Proxy.newProxyInstance(MBeanClient.class.getClassLoader(), new Class<?>[] { iface },
                        new MBeanClientInvocationHandler(this));
                proxies.put(iface, proxy);
            }
            return iface.cast(proxy);
        }
    }
    
    private <T> T execute(Action<T> action) throws JMException, ConnectorException {
        AdminClient adminClient = processInfo.getAdminClient();
        ObjectName cachedObjectName;
        synchronized (this) {
            cachedObjectName = this.cachedObjectName;
        }
        if (cachedObjectName != null) {
            try {
                return action.execute(adminClient, cachedObjectName);
            } catch (InstanceNotFoundException ex) {
                // Continue; we will attempt to re-resolve the object name
            }
        }
        cachedObjectName = internalGetObjectName();
        synchronized (this) {
            this.cachedObjectName = cachedObjectName;
        }
        if (log.isDebugEnabled()) {
            log.debug("Found MBean instance: " + cachedObjectName);
        }
        return action.execute(adminClient, cachedObjectName);
    }
    
    public Object invoke(final String operationName, final Object[] params, final String[] signature) throws JMException, ConnectorException {
        return execute(new Action<Object>() {
            public Object execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException {
                return adminClient.invoke(objectName, operationName, params, signature);
            }
        });
    }
    
    public Object getAttribute(final String attribute) throws JMException, ConnectorException {
        return execute(new Action<Object>() {
            public Object execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException {
                return adminClient.getAttribute(objectName, attribute);
            }
        });
    }
    
    public AttributeList getAttributes(final String[] attributes) throws JMException, ConnectorException {
        return execute(new Action<AttributeList>() {
            public AttributeList execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException {
                return adminClient.getAttributes(objectName, attributes);
            }
        });
    }
    
    /**
     * Check whether an MBean matching this client's {@link MBeanLocator} is registered in the MBean
     * server.
     * 
     * @return <code>true</code> if the MBean is registered, <code>false</code> otherwise
     * @throws JMException
     * @throws ConnectorException
     */
    public boolean isRegistered() throws JMException, ConnectorException {
        return execute(new Action<Boolean>() {
            public Boolean execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException {
                return adminClient.isRegistered(objectName);
            }
        });
    }
}
