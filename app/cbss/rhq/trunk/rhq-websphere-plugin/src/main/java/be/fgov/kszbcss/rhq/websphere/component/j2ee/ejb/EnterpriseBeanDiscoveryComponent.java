package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public abstract class EnterpriseBeanDiscoveryComponent implements ResourceDiscoveryComponent<EJBModuleComponent> {
    public final Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<EJBModuleComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        for (String name : context.getParentResourceComponent().getBeanNames(getType(), true)) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), name, name, null, getDescription(), null, null));
        }
        return result;
    }

    protected abstract EnterpriseBeanType getType();

    protected abstract String getDescription();
}
