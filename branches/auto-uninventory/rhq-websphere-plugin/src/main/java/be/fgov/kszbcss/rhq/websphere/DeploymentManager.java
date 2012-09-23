package be.fgov.kszbcss.rhq.websphere;

public class DeploymentManager extends WebSphereServer {
    public DeploymentManager(String cell, ProcessLocator processLocator) {
        super(cell, null, "dmgr", "DeploymentManager", processLocator);
    }
}
