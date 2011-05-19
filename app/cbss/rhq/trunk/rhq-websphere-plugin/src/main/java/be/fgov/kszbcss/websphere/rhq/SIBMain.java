package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface SIBMain {
    String[] showMessagingEngines() throws JMException, ConnectorException;
}
