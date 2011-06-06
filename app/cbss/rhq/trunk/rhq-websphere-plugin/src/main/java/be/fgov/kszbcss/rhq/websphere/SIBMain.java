package be.fgov.kszbcss.rhq.websphere;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface SIBMain {
    String[] showMessagingEngines() throws JMException, ConnectorException;
}
