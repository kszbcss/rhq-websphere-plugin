package be.fgov.kszbcss.rhq.websphere.component.server;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;

public class ThreadPoolDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        ManagedServer server = context.getParentResourceComponent().getServer();
        for (ThreadPoolConfiguration threadPool : server.queryConfig(new ThreadPoolQuery(server.getNode(), server.getServer()))) {
            String name = threadPool.getName();
            result.add(new DiscoveredResourceDetails(context.getResourceType(), name, name, null, "A thread pool.", null, null));
        }
        return result;
    }
}
