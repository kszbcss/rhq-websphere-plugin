package be.fgov.kszbcss.rhq.websphere.component.j2c;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryQuery;
import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryType;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;

public class ConnectionFactoryDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        ManagedServer server = context.getParentResourceComponent().getServer();
        for (String jndiName : server.queryConfig(new ConnectionFactoryQuery(server.getNode(), server.getServer(), ConnectionFactoryType.J2C)).getJndiNames()) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), jndiName, jndiName, null, "A J2C Connection Factory", null, null));
        }
        return result;
    }
}
