package be.fgov.kszbcss.websphere.rhq.support.measurement;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface PMIModuleSelector {
    String[] getPath() throws JMException, ConnectorException;
}
