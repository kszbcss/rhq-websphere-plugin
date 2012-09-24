package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface J2CMessageEndpoint {
    void pause() throws JMException, ConnectorException; 
    void resume() throws JMException, ConnectorException;
    Integer getStatus() throws JMException, ConnectorException;
    String getActivationProperties() throws JMException, ConnectorException;
}
