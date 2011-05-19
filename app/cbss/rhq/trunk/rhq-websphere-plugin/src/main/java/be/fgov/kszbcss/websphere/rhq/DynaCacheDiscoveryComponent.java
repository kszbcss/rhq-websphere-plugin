package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public class DynaCacheDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    private static final Log log = LogFactory.getLog(DynaCacheDiscoveryComponent.class); 
    
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        DynaCache cache = context.getParentResourceComponent().getServer().getMBeanClient("WebSphere:type=DynaCache,*").getProxy(DynaCache.class);
        for (String instanceName : cache.getCacheInstanceNames()) {
            if (instanceName.equals("baseCache") || instanceName.startsWith("ws/")) {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring DynaCache " + instanceName);
                }
            } else {
                result.add(new DiscoveredResourceDetails(context.getResourceType(), instanceName, instanceName, null, instanceName, null, null));
            }
        }
        return result;
    }
}
