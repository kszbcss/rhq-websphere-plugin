package be.fgov.kszbcss.websphere.rhq;

import java.util.HashMap;
import java.util.Map;

public abstract class ModuleInfoFactory {
    private static final Map<String,ModuleInfoFactory> factories;
    
    static {
        factories = new HashMap<String,ModuleInfoFactory>();
        factories.put("WebModuleDeployment", new ModuleInfoFactory(ModuleType.WEB) {
            @Override
            public String getDeploymentDescriptorPath() {
                return "WEB-INF/web.xml";
            }
        });
        factories.put("EJBModuleDeployment", new ModuleInfoFactory(ModuleType.EJB) {
            @Override
            public String getDeploymentDescriptorPath() {
                return "META-INF/ejb-jar.xml";
            }
        });
    }
    
    private final ModuleType type;
    
    public ModuleInfoFactory(ModuleType type) {
        this.type = type;
    }
    
    public static ModuleInfoFactory getInstance(String configDataType) {
        return factories.get(configDataType);
    }
    
    public abstract String getDeploymentDescriptorPath();
    
    public ModuleInfo create(String name, byte[] deploymentDescriptor) {
        return new ModuleInfo(type, name, deploymentDescriptor);
    }
}
