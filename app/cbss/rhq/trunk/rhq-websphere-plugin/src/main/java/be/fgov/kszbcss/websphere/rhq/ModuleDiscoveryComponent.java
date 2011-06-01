package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public abstract class ModuleDiscoveryComponent implements ResourceDiscoveryComponent<ApplicationComponent> {
    private static final Log log = LogFactory.getLog(ModuleDiscoveryComponent.class);
    
    public final Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<ApplicationComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        ApplicationInfo applicationInfo = context.getParentResourceComponent().getApplicationInfo();
        List<ModuleInfo> modules = applicationInfo.getModules(getModuleType());
        if (log.isDebugEnabled()) {
            log.debug("Found " + modules.size() + " module(s) of type " + getModuleType() + " in application " + context.getParentResourceComponent().getApplicationName());
        }
        for (ModuleInfo module : modules) {
            String name = module.getName();
            result.add(new DiscoveredResourceDetails(context.getResourceType(), name, name, null, getDescription(name), null, null));
        }
        return result;
    }
    
    protected abstract ModuleType getModuleType();
    protected abstract String getDescription(String moduleName);
}
