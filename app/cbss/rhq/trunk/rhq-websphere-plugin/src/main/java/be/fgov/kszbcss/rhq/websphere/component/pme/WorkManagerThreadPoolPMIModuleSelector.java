package be.fgov.kszbcss.rhq.websphere.component.pme;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiConstants;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIModuleSelector;

public class WorkManagerThreadPoolPMIModuleSelector implements PMIModuleSelector {
    private final ManagedServer server;
    private String jndiName;

    public WorkManagerThreadPoolPMIModuleSelector(ManagedServer server, String jndiName) {
        this.server = server;
        this.jndiName = jndiName;
    }

    public String[] getPath() throws JMException, ConnectorException {
        String name = server.queryConfig(new WorkManagerMapQuery(server.getNode(), server.getServer())).get(jndiName);
        if (name == null) {
            throw new JMException("No work manager found for JNDI name " + jndiName);
        }
        return new String[] { PmiConstants.THREADPOOL_MODULE, "WorkManager." + name };
    }
}
