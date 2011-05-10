package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EJBModuleDiscoveryComponent implements ResourceDiscoveryComponent<ApplicationComponent>  {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<ApplicationComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        Document descriptor = context.getParentResourceComponent().getDeploymentDescriptor();
        for (Element module : Utils.getElements(descriptor.getDocumentElement(), "module")) {
            Element ejb = Utils.getFirstElement(module, "ejb");
            if (ejb != null) {
                String name = ejb.getTextContent();
                result.add(new DiscoveredResourceDetails(context.getResourceType(), name, name, null, "An EJB module.", null, null));
            }
        }
        return result;
    }
}
