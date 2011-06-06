package be.fgov.kszbcss.rhq.websphere;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface SIBMessagingEngine {
    String getHealth() throws JMException, ConnectorException;
}
