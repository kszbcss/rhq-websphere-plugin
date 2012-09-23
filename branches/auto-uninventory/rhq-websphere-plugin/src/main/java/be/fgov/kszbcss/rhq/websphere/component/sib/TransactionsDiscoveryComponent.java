package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.Collections;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public class TransactionsDiscoveryComponent implements ResourceDiscoveryComponent<SIBMessagingEngineComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<SIBMessagingEngineComponent> context) throws InvalidPluginConfigurationException, Exception {
        return Collections.singleton(new DiscoveredResourceDetails(context.getResourceType(),
                "Transactions", "Transactions", null,
                "Provides global statistics about SIB messaging engine transactions", null, null));
    }
}
