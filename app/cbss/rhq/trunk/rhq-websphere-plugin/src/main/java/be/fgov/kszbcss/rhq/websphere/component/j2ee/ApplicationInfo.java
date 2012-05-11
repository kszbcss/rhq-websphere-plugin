package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

/**
 * Contains the static information about an application, in particular the module structure and the
 * deployment descriptors.
 */
public class ApplicationInfo implements Serializable {
    private static final long serialVersionUID = -8427058440507167719L;
    
    private final DeploymentDescriptor deploymentDescriptor;
    private final ModuleInfo[] modules;
    
    public ApplicationInfo(byte[] deploymentDescriptor, ModuleInfo[] modules) {
        this.deploymentDescriptor = new DeploymentDescriptor(deploymentDescriptor);
        this.modules = modules;
    }
    
    public Document getDeploymentDescriptor() {
        return deploymentDescriptor.getDOM();
    }
    
    public List<ModuleInfo> getModules(ModuleType type) {
        List<ModuleInfo> result = new ArrayList<ModuleInfo>();
        for (ModuleInfo module : modules) {
            if (module.getType() == type) {
                result.add(module);
            }
        }
        return result;
    }
    
    public ModuleInfo getModule(String name) {
        for (ModuleInfo module : modules) {
            if (module.getName().equals(name)) {
                return module;
            }
        }
        return null;
    }
}
