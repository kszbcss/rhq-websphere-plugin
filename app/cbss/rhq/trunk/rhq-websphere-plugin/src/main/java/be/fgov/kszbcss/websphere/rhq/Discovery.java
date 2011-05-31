package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.ws.management.discovery.ServerInfo;

/**
 * Proxy interface for the <tt>Discovery</tt> MBean.
 */
public interface Discovery {
    ServerInfo getParent() throws JMException, ConnectorException;
}
