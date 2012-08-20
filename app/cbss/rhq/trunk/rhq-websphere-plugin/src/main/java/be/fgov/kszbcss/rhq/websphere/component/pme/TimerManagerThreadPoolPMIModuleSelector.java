package be.fgov.kszbcss.rhq.websphere.component.pme;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiConstants;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIModuleSelector;

public class TimerManagerThreadPoolPMIModuleSelector implements PMIModuleSelector {
    private final ManagedServer server;
    private String jndiName;

    public TimerManagerThreadPoolPMIModuleSelector(ManagedServer server, String jndiName) {
        this.server = server;
        this.jndiName = jndiName;
    }

    public String[] getPath() throws JMException, ConnectorException, InterruptedException {
        String name = server.queryConfig(new TimerManagerMapQuery(server.getNode(), server.getServer()), false).get(jndiName);
        if (name == null) {
            throw new JMException("No timer manager found for JNDI name " + jndiName);
        }
        return new String[] { PmiConstants.THREADPOOL_MODULE, name + ".Alarm Pool" };
    }
}
