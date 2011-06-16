package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;

public class ApplicationDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        ManagedServer server = context.getParentResourceComponent().getServer();
        for (String applicationName : server.queryConfig(new DeployedApplicationsQuery(server.getNode(), server.getServer()))) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), applicationName, applicationName, null, "An enterprise application.", null, null));
        }
        return result;
    }
}
