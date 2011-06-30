package be.fgov.kszbcss.rhq.websphere.component.sib;

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

public class SIBMessagingEngineDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    private static final Log log = LogFactory.getLog(SIBMessagingEngineDiscoveryComponent.class);
    
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        ManagedServer server = context.getParentResourceComponent().getServer();
        log.debug("Retrieving list of messaging engines");
        for (SIBMessagingEngineInfo me : server.queryConfig(new SIBMessagingEngineQuery(server.getNode(), server.getServer()))) {
            if (log.isDebugEnabled()) {
                log.debug("Found messaging engine " + me.getName() + " (bus " + me.getBusName() + ")");
            }
            result.add(new DiscoveredResourceDetails(context.getResourceType(), me.getName(), me.getName(), null, "Messaging engine for bus " + me.getBusName(), null, null));
        }
        return result;
    }
}
