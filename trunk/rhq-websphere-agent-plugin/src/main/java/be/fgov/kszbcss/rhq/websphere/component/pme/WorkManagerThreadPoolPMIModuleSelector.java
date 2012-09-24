package be.fgov.kszbcss.rhq.websphere.component.pme;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiConstants;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIModuleSelector;

public class WorkManagerThreadPoolPMIModuleSelector implements PMIModuleSelector {
    private final ApplicationServer server;
    private String jndiName;

    public WorkManagerThreadPoolPMIModuleSelector(ApplicationServer server, String jndiName) {
        this.server = server;
        this.jndiName = jndiName;
    }

    public String[] getPath() throws JMException, ConnectorException, InterruptedException {
        String name = server.queryConfig(new WorkManagerMapQuery(server.getNode(), server.getServer()), false).get(jndiName);
        if (name == null) {
            throw new JMException("No work manager found for JNDI name " + jndiName);
        }
        return new String[] { PmiConstants.THREADPOOL_MODULE, "WorkManager." + name };
    }
}
