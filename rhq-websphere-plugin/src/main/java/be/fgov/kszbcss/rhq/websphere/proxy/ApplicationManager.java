package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Proxy interface for the <tt>ApplicationManager</tt> MBean.
 */
public interface ApplicationManager {
    void startApplication(String applicationName) throws JMException, ConnectorException;
    void stopApplication(String applicationName) throws JMException, ConnectorException;
}
