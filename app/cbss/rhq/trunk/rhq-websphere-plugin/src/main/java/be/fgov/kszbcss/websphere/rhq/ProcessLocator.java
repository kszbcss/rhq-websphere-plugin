package be.fgov.kszbcss.websphere.rhq;

import java.util.Properties;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface ProcessLocator {
    void getAdminClientProperties(Properties properties) throws JMException, ConnectorException;
}
