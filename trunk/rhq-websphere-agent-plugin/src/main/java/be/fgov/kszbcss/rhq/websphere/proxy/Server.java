package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface Server {
    void restart() throws JMException, ConnectorException;
}
