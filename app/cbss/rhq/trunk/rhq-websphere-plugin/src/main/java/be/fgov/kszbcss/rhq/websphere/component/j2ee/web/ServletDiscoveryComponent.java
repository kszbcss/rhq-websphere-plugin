package be.fgov.kszbcss.rhq.websphere.component.j2ee.web;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;


public class ServletDiscoveryComponent implements ResourceDiscoveryComponent<WebModuleComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebModuleComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        for (String servletName : context.getParentResourceComponent().getServletNames()) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), servletName, servletName, null, "A servlet.", null, null));
        }
        return result;
    }
}
