package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public abstract class SIBLocalizationPointDiscoveryComponent implements ResourceDiscoveryComponent<SIBMessagingEngineComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<SIBMessagingEngineComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        SIBMessagingEngineComponent me = context.getParentResourceComponent();
        SIBMessagingEngineInfo meInfo = me.getInfo(true);
        if (meInfo == null) {
            throw new InvalidPluginConfigurationException("Messaging engine " + me.getName() + " not found");
        }
        for (String destination : meInfo.getDestinationNames(getType())) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), destination, destination, null, "SIBus Destination", null, null));
        }
        return result;
    }

    protected abstract SIBLocalizationPointType getType();
}
