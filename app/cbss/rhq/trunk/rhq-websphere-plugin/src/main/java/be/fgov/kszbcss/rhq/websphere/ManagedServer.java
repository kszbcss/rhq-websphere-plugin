package be.fgov.kszbcss.rhq.websphere;

import javax.management.JMException;

import org.rhq.core.domain.configuration.Configuration;

import be.fgov.kszbcss.rhq.websphere.component.server.ClusterNameQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryService;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryServiceFactory;

import com.ibm.websphere.management.exception.ConnectorException;

public class ManagedServer extends ApplicationServer {
    private NodeAgent nodeAgent;
    
    public ManagedServer(String cell, String node, String server, Configuration config) {
        super(cell, node, server, "ManagedProcess", new ConfigurationBasedProcessLocator(config));
    }

    public synchronized NodeAgent getNodeAgent() throws ConnectorException {
        if (nodeAgent == null) {
            nodeAgent = new NodeAgent(getCell(), getNode(), new ParentProcessLocator(this));
        }
        return nodeAgent;
    }
    
    @Override
    protected ConfigQueryService createConfigQueryService() throws ConnectorException {
        return ConfigQueryServiceFactory.getInstance().getConfigQueryService(getCell(), getNodeAgent().getDeploymentManager());
    }

    public String getClusterName() throws InterruptedException, JMException, ConnectorException {
        return queryConfig(new ClusterNameQuery(getNode(), getServer()), false);
    }
}
