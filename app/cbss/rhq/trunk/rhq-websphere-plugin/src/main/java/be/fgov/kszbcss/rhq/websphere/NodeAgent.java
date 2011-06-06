package be.fgov.kszbcss.rhq.websphere;

public class NodeAgent extends WebSphereServer {
    private DeploymentManager deploymentManager;
    
    public NodeAgent(ProcessLocator processLocator) {
        super(processLocator);
    }

    public synchronized DeploymentManager getDeploymentManager() {
        if (deploymentManager == null) {
            deploymentManager = new DeploymentManager(new ParentProcessLocator(this));
        }
        return deploymentManager;
    }
}
