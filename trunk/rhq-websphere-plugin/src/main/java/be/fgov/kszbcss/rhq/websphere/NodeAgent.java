package be.fgov.kszbcss.rhq.websphere;

import com.ibm.websphere.management.exception.ConnectorException;

public class NodeAgent extends WebSphereServer {
    private DeploymentManager deploymentManager;
    
    public NodeAgent(String cell, String node, ProcessLocator processLocator) {
        super(cell, node, "nodeagent", "NodeAgent", processLocator);
    }

    public synchronized DeploymentManager getDeploymentManager() throws ConnectorException {
        if (deploymentManager == null) {
            deploymentManager = new DeploymentManager(getCell(), new ParentProcessLocator(this));
        }
        return deploymentManager;
    }
}
