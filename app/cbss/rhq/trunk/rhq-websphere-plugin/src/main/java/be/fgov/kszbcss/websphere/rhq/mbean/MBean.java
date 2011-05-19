package be.fgov.kszbcss.websphere.rhq.mbean;

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
// TODO: we could actually cache instances of this class in WebSphereServer
public class MBean {
    private static final Log log = LogFactory.getLog(MBean.class.getName());
    
    private interface Action<T> {
        public T execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException;
    }
    
    private final WebSphereServer server;
    private final MBeanLocator locator;
    private ObjectName cachedObjectName;
    
    public MBean(WebSphereServer server, MBeanLocator locator) {
        this.server = server;
        this.locator = locator;
    }
    
    public MBean(WebSphereServer server, ObjectName objectNamePattern) {
        this(server, new MBeanObjectNamePatternLocator(objectNamePattern));
    }
    
    public MBeanLocator getLocator() {
        return locator;
    }

    public ObjectName getObjectName() throws JMException, ConnectorException {
        return locator.locate(server.getAdminClient());
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
