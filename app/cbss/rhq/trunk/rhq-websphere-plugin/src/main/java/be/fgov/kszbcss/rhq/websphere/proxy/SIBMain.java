package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface SIBMain {
    String[] showMessagingEngines() throws JMException, ConnectorException;
}
