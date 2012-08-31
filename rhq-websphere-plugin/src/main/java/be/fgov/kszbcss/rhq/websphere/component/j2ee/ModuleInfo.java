package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.io.Serializable;

import org.w3c.dom.Document;

public class ModuleInfo implements Serializable {
    private static final long serialVersionUID = -4670813651457441678L;
    
    private final ModuleType type;
    private final String name;
    private final DeploymentDescriptor deploymentDescriptor;
    
    public ModuleInfo(ModuleType type, String name, byte[] deploymentDescriptor) {
        this.type = type;
        this.name = name;
        this.deploymentDescriptor = new DeploymentDescriptor(deploymentDescriptor);
    }

    public ModuleType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public Document getDeploymentDescriptor() {
        return deploymentDescriptor.getDOM();
    }
}
