package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;

public class ApplicationDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    private static final Log log = LogFactory.getLog(ApplicationDiscoveryComponent.class);
    
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        ManagedServer server = context.getParentResourceComponent().getServer();
        String[] applicationNames = server.queryConfig(new DeployedApplicationsQuery(server.getNode(), server.getServer()));
        if (log.isDebugEnabled()) {
            log.debug("Discovered the following applications on " + context.getParentResourceComponent().getResourceContext().getResourceKey() + ": " + Arrays.asList(applicationNames));
        }
        for (String applicationName : applicationNames) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), applicationName, applicationName, null, "An enterprise application.", null, null));
        }
        return result;
    }
}
