package be.fgov.kszbcss.rhq.websphere.component.server.log.xm;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.xm.logging.ExtendedLogMessage;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Proxy interface for the <tt>ExtendedLoggingService</tt> MBean exposed by the WebSphere Extended
 * Monitoring application.
 */
public interface ExtendedLoggingService {
    long getNextSequence();
    ExtendedLogMessage[] getMessages(long startSequence) throws JMException, ConnectorException;
}
