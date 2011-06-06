package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.ws.management.discovery.ServerInfo;

/**
 * Proxy interface for the <tt>Discovery</tt> MBean.
 */
public interface Discovery {
    ServerInfo getParent() throws JMException, ConnectorException;
}
