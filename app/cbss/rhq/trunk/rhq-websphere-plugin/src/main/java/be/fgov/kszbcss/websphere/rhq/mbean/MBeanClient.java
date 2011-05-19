package be.fgov.kszbcss.websphere.rhq.mbean;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.websphere.rhq.WebSphereServer;

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
    
    private final WebSphereServer server;
    private final MBeanLocator locator;
    private final Map<Class<?>,Object> proxies = new HashMap<Class<?>,Object>();
    private ObjectName cachedObjectName;
    
    MBeanClient(WebSphereServer server, MBeanLocator locator) {
        this.server = server;
        this.locator = locator;
    }
    
    public MBeanLocator getLocator() {
        return locator;
    }

    // TODO: need to decide in which case we can return the cached object name here
    public ObjectName getObjectName() throws JMException, ConnectorException {
        return locator.locate(server.getAdminClient());
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
        AdminClient adminClient = server.getAdminClient();
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
        if (log.isDebugEnabled()) {
            log.debug("Attempting to resolve " + locator);
        }
        cachedObjectName = getObjectName();
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
}
