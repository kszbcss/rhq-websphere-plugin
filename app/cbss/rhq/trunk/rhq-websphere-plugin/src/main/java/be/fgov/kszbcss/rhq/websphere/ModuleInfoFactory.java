package be.fgov.kszbcss.rhq.websphere;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigServiceWrapper;

public class ModuleInfoFactory {
    private static final Log log = LogFactory.getLog(ModuleInfoFactory.class);
    
    private static final Map<String,ModuleInfoFactory> factories;
    
    static {
        factories = new HashMap<String,ModuleInfoFactory>();
        factories.put("WebModuleDeployment", new ModuleInfoFactory(ModuleType.WEB, "WEB-INF", "web"));
        factories.put("EJBModuleDeployment", new ModuleInfoFactory(ModuleType.EJB, "META-INF", "ejb-jar"));
    }
    
    private final ModuleType type;
    private final String infPath;
    private final String deploymentDescriptorName;
    
    public ModuleInfoFactory(ModuleType type, String infPath, String deploymentDescriptorName) {
        this.type = type;
        this.infPath = infPath;
        this.deploymentDescriptorName = deploymentDescriptorName;
    }

    public static ModuleInfoFactory getInstance(String configDataType) {
        return factories.get(configDataType);
    }
    
    public String locateDeploymentDescriptor(ConfigServiceWrapper configService, String moduleURI) throws JMException, ConnectorException {
        if (configService.getWebSphereVersion().startsWith("6.")) {
            log.debug("Server implements J2EE 1.4; returning URI of static deployment descriptor");
        } else {
            String[] resources = configService.listResourceNames(moduleURI + "/" + infPath, 1, 1);
            if (log.isDebugEnabled()) {
                log.debug("Deployment descriptor list: " + Arrays.asList(resources));
            }
            String merged = search(resources, "/" + deploymentDescriptorName + "_merged.xml");
            if (merged != null) {
                log.debug("Merged deployment descriptor found");
                return merged;
            }
        }
        return moduleURI + "/" + infPath + "/" + deploymentDescriptorName + ".xml";
    }
    
    private static String search(String[] list, String suffix) {
        for (String item : list) {
            if (item.endsWith(suffix)) {
                return item;
            }
        }
        return null;
    }
    
    public ModuleInfo create(String name, byte[] deploymentDescriptor) {
        return new ModuleInfo(type, name, deploymentDescriptor);
    }
}
