package be.fgov.kszbcss.websphere.rhq;

import java.util.Collections;
import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ManualAddFacet;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public class WebSphereDiscoveryComponent implements ResourceDiscoveryComponent, ManualAddFacet {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext context)
            throws InvalidPluginConfigurationException, Exception {
        // Currently we don't support auto discovery of WebSphere instances
        return Collections.emptySet();
    }

    public DiscoveredResourceDetails discoverResource(Configuration pluginConfig,
            ResourceDiscoveryContext context)
            throws InvalidPluginConfigurationException {
        String key = pluginConfig.getSimpleValue("host", null) + ":" + pluginConfig.getSimpleValue("port", null);
        return new DiscoveredResourceDetails(context.getResourceType(),
                key, "WebSphere Application Server", null, key, pluginConfig, null);
    }
}
