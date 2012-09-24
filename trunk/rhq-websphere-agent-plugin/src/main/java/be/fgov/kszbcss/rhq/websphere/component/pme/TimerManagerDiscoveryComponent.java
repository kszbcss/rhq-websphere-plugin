package be.fgov.kszbcss.rhq.websphere.component.pme;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;

public class TimerManagerDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        ApplicationServer server = context.getParentResourceComponent().getServer();
        for (String jndiName : server.queryConfig(new TimerManagerMapQuery(server.getNode(), server.getServer()), true).keySet()) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), jndiName, jndiName, null, "A timer manager.", null, null));
        }
        return result;
    }
}
