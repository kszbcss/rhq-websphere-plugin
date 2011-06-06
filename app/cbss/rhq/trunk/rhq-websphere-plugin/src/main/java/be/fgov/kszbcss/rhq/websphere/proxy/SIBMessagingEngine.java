package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface SIBMessagingEngine {
    String getHealth() throws JMException, ConnectorException;
}
