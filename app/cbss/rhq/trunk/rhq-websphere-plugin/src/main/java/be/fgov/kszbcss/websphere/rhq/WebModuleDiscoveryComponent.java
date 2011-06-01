package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public class WebModuleDiscoveryComponent implements ResourceDiscoveryComponent<ApplicationComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<ApplicationComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        ApplicationInfo applicationInfo = context.getParentResourceComponent().getApplicationInfo();
        for (ModuleInfo module : applicationInfo.getModules(ModuleType.WEB)) {
            String name = module.getName();
            result.add(new DiscoveredResourceDetails(context.getResourceType(), name, name, null, "A Web module.", null, null));
        }
        return result;
    }
}
