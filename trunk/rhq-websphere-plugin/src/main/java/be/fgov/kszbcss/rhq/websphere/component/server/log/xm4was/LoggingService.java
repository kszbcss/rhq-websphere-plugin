package be.fgov.kszbcss.rhq.websphere.component.server.log.xm4was;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Proxy interface for the <tt>ExtendedLoggingService</tt> MBean exposed by the WebSphere Extended
 * Monitoring application.
 */
public interface LoggingService {
    long getNextSequence() throws JMException, ConnectorException;
    String[] getMessages(long startSequence, int maxMessageSize) throws JMException, ConnectorException;
}
