package be.fgov.kszbcss.websphere.rhq.support.measurement;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public class StaticPMIModuleSelector implements PMIModuleSelector {
    private final String[] path;

    public StaticPMIModuleSelector(String[] path) {
        this.path = path;
    }

    public String[] getPath() throws JMException, ConnectorException {
        return path;
    }
}
