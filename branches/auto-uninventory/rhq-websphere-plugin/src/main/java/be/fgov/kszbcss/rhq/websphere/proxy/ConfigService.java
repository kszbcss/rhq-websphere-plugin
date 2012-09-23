package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.ObjectName;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Proxy interface for the <tt>ConfigService</tt> MBean.
 */
public interface ConfigService {
    ObjectName[] resolve(Session session, String containmentPath) throws JMException, ConnectorException;
    Object getAttribute(Session session, ObjectName parent, String attributeName) throws JMException, ConnectorException;
    AttributeList getAttributes(Session session, ObjectName parent, String[] attributes, boolean recursive) throws JMException, ConnectorException;
    void discard(Session session) throws JMException, ConnectorException;
}
