package be.fgov.kszbcss.websphere.rhq.connector;

import java.util.Properties;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.exception.ConnectorException;

public abstract class AdminClientWrapper implements AdminClient {
    private final AdminClient target;

    public AdminClientWrapper(AdminClient target) {
        this.target = target;
    }

    public void addNotificationListener(ObjectName name,
            NotificationListener listener, NotificationFilter filter,
            Object handback) throws InstanceNotFoundException,
            ConnectorException {
        target.addNotificationListener(name, listener, filter, handback);
    }

    public void addNotificationListener(ObjectName name, ObjectName listener,
            NotificationFilter filter, Object handback)
            throws ConnectorException, InstanceNotFoundException {
        target.addNotificationListener(name, listener, filter, handback);
    }

    public void addNotificationListenerExtended(ObjectName name,
            NotificationListener listener, NotificationFilter filter,
            Object handback) throws ConnectorException {
        target.addNotificationListenerExtended(name, listener, filter,
                        handback);
    }

    public Object getAttribute(ObjectName name, String attribute)
            throws MBeanException, AttributeNotFoundException,
            InstanceNotFoundException, ReflectionException, ConnectorException {
        return target.getAttribute(name, attribute);
    }

    public AttributeList getAttributes(ObjectName name, String[] attributes)
            throws InstanceNotFoundException, ReflectionException,
            ConnectorException {
        return target.getAttributes(name, attributes);
    }

    public ClassLoader getClassLoader(ObjectName name)
            throws ConnectorException, InstanceNotFoundException {
        return target.getClassLoader(name);
    }

    public ClassLoader getClassLoaderFor(ObjectName name)
            throws ConnectorException, InstanceNotFoundException {
        return target.getClassLoaderFor(name);
    }

    public Properties getConnectorProperties() {
        return target.getConnectorProperties();
    }

    public String getDefaultDomain() throws ConnectorException {
        return target.getDefaultDomain();
    }

    public String getDomainName() throws ConnectorException {
        return target.getDomainName();
    }

    public Integer getMBeanCount() throws ConnectorException {
        return target.getMBeanCount();
    }

    public MBeanInfo getMBeanInfo(ObjectName name)
            throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, ConnectorException {
        return target.getMBeanInfo(name);
    }

    public ObjectInstance getObjectInstance(ObjectName objectName)
            throws ConnectorException, InstanceNotFoundException {
        return target.getObjectInstance(objectName);
    }

    public ObjectName getServerMBean() throws ConnectorException {
        return target.getServerMBean();
    }

    public String getType() {
        return target.getType();
    }

    public Object invoke(ObjectName name, String operationName,
            Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException,
            ReflectionException, ConnectorException {
        return target.invoke(name, operationName, params, signature);
    }

    public Session isAlive() throws ConnectorException {
        return target.isAlive();
    }

    public Session isAlive(int timeout) throws ConnectorException {
        return target.isAlive(timeout);
    }

    public boolean isInstanceOf(ObjectName name, String className)
            throws InstanceNotFoundException, ConnectorException {
        return target.isInstanceOf(name, className);
    }

    public boolean isRegistered(ObjectName name) throws ConnectorException {
        return target.isRegistered(name);
    }

    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query)
            throws ConnectorException {
        return target.queryMBeans(name, query);
    }

    public Set<ObjectName> queryNames(ObjectName name, QueryExp query)
            throws ConnectorException {
        return target.queryNames(name, query);
    }

    public void removeNotificationListener(ObjectName name,
            NotificationListener listener) throws InstanceNotFoundException,
            ListenerNotFoundException, ConnectorException {
        target.removeNotificationListener(name, listener);
    }

    public void removeNotificationListener(ObjectName name,
            ObjectName listener, NotificationFilter filter, Object handback)
            throws ConnectorException, InstanceNotFoundException,
            ListenerNotFoundException {
        target.removeNotificationListener(name, listener, filter, handback);
    }

    public void removeNotificationListener(ObjectName name, ObjectName listener)
            throws ConnectorException, InstanceNotFoundException,
            ListenerNotFoundException {
        target.removeNotificationListener(name, listener);
    }

    public void removeNotificationListenerExtended(NotificationListener listener)
            throws ListenerNotFoundException, ConnectorException {
        target.removeNotificationListenerExtended(listener);
    }

    public void removeNotificationListenerExtended(ObjectName name,
            NotificationListener listener) throws ListenerNotFoundException,
            ConnectorException {
        target.removeNotificationListenerExtended(name, listener);
    }

    public void setAttribute(ObjectName name, Attribute attribute)
            throws InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException,
            ReflectionException, ConnectorException {
        target.setAttribute(name, attribute);
    }

    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
            throws InstanceNotFoundException, ReflectionException,
            ConnectorException {
        return target.setAttributes(name, attributes);
    }
}
