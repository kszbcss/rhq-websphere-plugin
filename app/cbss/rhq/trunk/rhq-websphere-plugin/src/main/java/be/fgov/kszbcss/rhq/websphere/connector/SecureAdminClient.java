package be.fgov.kszbcss.rhq.websphere.connector;

import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.io.IOUtils;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.management.repository.DocumentContentSource;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;
import com.ibm.ws.management.AdminDataHolder;

public class SecureAdminClient extends AdminClientWrapper {
    private final Subject subject;
    
    public SecureAdminClient(AdminClient target, Subject subject) {
        super(target);
        this.subject = subject;
    }

    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                super.addNotificationListener(name, listener, filter, handback);
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
                super.addNotificationListener(name, listener, filter, handback);
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
                super.addNotificationListenerExtended(name, listener, filter, handback);
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
                return super.getAttribute(name, attribute);
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
                return super.getAttributes(name, attributes);
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
                return super.getClassLoader(name);
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
                return super.getClassLoaderFor(name);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public String getDefaultDomain() throws ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return super.getDefaultDomain();
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
                return super.getDomainName();
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
                return super.getMBeanCount();
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
                return super.getMBeanInfo(name);
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
                return super.getObjectInstance(objectName);
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
                return super.getServerMBean();
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException, ConnectorException {
        Object result;
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                result = super.invoke(name, operationName, params, signature);
                // TODO: quick and dirty hack
                if (result instanceof DocumentContentSource) {
                    // FileTransferClientImpl (which is used by the input stream returned by
                    // DocumentContentSource) uses the subject stored in the global AdminDataHolder.
                    // This breaks thread isolation. As a workaround, we synchronize access and
                    // set the cached subject explicitly.
                    synchronized (AdminDataHolder.class) {
                        AdminDataHolder.setData(AdminDataHolder.WSSUBJECT, subject);
                        try {
                            try {
                                InputStream in = ((DocumentContentSource)result).getSource();
                                try {
                                    return IOUtils.toByteArray(in);
                                } finally {
                                    in.close();
                                }
                            } catch (IOException ex) {
                                throw new MBeanException(ex);
                            }
                        } finally {
                            AdminDataHolder.removeData(AdminDataHolder.WSSUBJECT);
                        }
                    }
                } else {
                    return result;
                }
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
                return super.isAlive();
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
                return super.isAlive(timeout);
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
                return super.isInstanceOf(name, className);
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
                return super.isRegistered(name);
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
                return super.queryMBeans(name, query);
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
                return super.queryNames(name, query);
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
                super.removeNotificationListener(name, listener);
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
                super.removeNotificationListener(name, listener);
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
                super.removeNotificationListener(name, listener, filter, handback);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public void removeNotificationListenerExtended(NotificationListener listener) throws ListenerNotFoundException, ConnectorException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                super.removeNotificationListenerExtended(listener);
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
                super.removeNotificationListenerExtended(name, listener);
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
                super.setAttribute(name, attribute);
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
                return super.setAttributes(name, attributes);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
}
