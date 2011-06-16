package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ApplicationInfo implements Serializable {
    private static final long serialVersionUID = -8427058440507167720L;
    
    private final ModuleInfo[] modules;
    
    public ApplicationInfo(ModuleInfo[] modules) {
        this.modules = modules;
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
