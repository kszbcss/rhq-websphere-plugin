package be.fgov.kszbcss.websphere.rhq;

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
import javax.security.auth.Subject;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;

public class SecureAdminClient implements AdminClient {
    private final AdminClient target;
    private final Subject subject;
    
    public SecureAdminClient(AdminClient target, Subject subject) {
        this.target = target;
        this.subject = subject;
    }

    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.addNotificationListener(name, listener, filter, handback);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws ConnectorException, InstanceNotFoundException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.addNotificationListener(name, listener, filter, handback);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public void addNotificationListenerExtended(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.addNotificationListenerExtended(name, listener, filter, handback);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.getAttribute(name, attribute);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.getAttributes(name, attributes);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public ClassLoader getClassLoader(ObjectName name) throws ConnectorException, InstanceNotFoundException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.getClassLoader(name);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public ClassLoader getClassLoaderFor(ObjectName name) throws ConnectorException, InstanceNotFoundException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.getClassLoaderFor(name);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public Properties getConnectorProperties() {
        return target.getConnectorProperties();
    }
    
    public String getDefaultDomain() throws ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.getDefaultDomain();
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public String getDomainName() throws ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.getDomainName();
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public Integer getMBeanCount() throws ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.getMBeanCount();
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.getMBeanInfo(name);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public ObjectInstance getObjectInstance(ObjectName objectName) throws ConnectorException, InstanceNotFoundException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.getObjectInstance(objectName);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public ObjectName getServerMBean() throws ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.getServerMBean();
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public String getType() {
        return target.getType();
    }
    
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.invoke(name, operationName, params, signature);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public Session isAlive() throws ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.isAlive();
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public Session isAlive(int timeout) throws ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.isAlive(timeout);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.isInstanceOf(name, className);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public boolean isRegistered(ObjectName name) throws ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.isRegistered(name);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) throws ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.queryMBeans(name, query);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query) throws ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.queryNames(name, query);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.removeNotificationListener(name, listener);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public void removeNotificationListener(ObjectName name, ObjectName listener) throws ConnectorException, InstanceNotFoundException, ListenerNotFoundException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.removeNotificationListener(name, listener);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws ConnectorException, InstanceNotFoundException, ListenerNotFoundException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.removeNotificationListener(name, listener, filter, handback);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    @SuppressWarnings("deprecation")
    public void removeNotificationListenerExtended(NotificationListener listener) throws ListenerNotFoundException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.removeNotificationListenerExtended(listener);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public void removeNotificationListenerExtended(ObjectName name, NotificationListener listener) throws ListenerNotFoundException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.removeNotificationListenerExtended(name, listener);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.setAttribute(name, attribute);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.setAttributes(name, attributes);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
}
