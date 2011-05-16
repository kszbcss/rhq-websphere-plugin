package be.fgov.kszbcss.websphere.rhq.connector;

import java.util.Collections;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public class AdminClientStatsDiscoveryComponent implements ResourceDiscoveryComponent<ResourceComponent<?>> {
    public Set<DiscoveredResourceDetails> discoverResources(
            ResourceDiscoveryContext<ResourceComponent<?>> context)
            throws InvalidPluginConfigurationException, Exception {
        return Collections.singleton(new DiscoveredResourceDetails(context.getResourceType(),
                "AdminClientStats", "WebSphere Connector Subsystem", null,
                "Provides global statistics about WebSphere connector (AdminClient) instances", null, null));
    }
}
