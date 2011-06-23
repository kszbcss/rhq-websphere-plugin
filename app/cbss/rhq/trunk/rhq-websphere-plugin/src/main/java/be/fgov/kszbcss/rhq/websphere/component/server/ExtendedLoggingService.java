package be.fgov.kszbcss.rhq.websphere.component.server;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.xm.logging.ExtendedLogMessage;

import com.ibm.websphere.management.exception.ConnectorException;

public interface ExtendedLoggingService {
    ExtendedLogMessage[] getMessages(long startSequence) throws JMException, ConnectorException;
}
