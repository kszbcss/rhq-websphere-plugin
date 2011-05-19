package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public class SIBMessagingEngineDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    private static final Log log = LogFactory.getLog(SIBMessagingEngineDiscoveryComponent.class);
    
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        WebSphereServer server = context.getParentResourceComponent().getServer();
        log.debug("Retrieving list of messaging engines");
        for (String line : server.getMBeanClient("WebSphere:type=SIBMain,*").getProxy(SIBMain.class).showMessagingEngines()) {
            String[] parts = line.split(":");
            String busName = parts[0];
            String meName = parts[1];
            if (log.isDebugEnabled()) {
                log.debug("Found messaging engine " + meName + " (bus " + busName + ")");
            }
            result.add(new DiscoveredResourceDetails(context.getResourceType(), meName, meName, null, "Messaging engine for bus " + busName, null, null));
        }
        return result;
    }
}
