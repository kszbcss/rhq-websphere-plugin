package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public class DynaCacheDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        MBean mbean = new MBean(context.getParentResourceComponent().getServer(), Utils.createObjectName("WebSphere:type=DynaCache,*"));
        for (String instanceName : (String[])mbean.invoke("getCacheInstanceNames", new Object[0], new String[0])) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), instanceName, instanceName, null, instanceName, null, null));
        }
        return result;
    }
}
