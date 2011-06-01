package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.w3c.dom.Element;

public class MessageDrivenBeanDiscoveryComponent implements ResourceDiscoveryComponent<EJBModuleComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<EJBModuleComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        Element beans = Utils.getFirstElement(context.getParentResourceComponent().getModuleInfo().getDeploymentDescriptor().getDocumentElement(), "enterprise-beans");
        if (beans != null) {
            for (Element bean : Utils.getElements(beans, "message-driven")) {
                String name = Utils.getFirstElement(bean, "ejb-name").getTextContent();
                result.add(new DiscoveredResourceDetails(context.getResourceType(), name, name, null, "A message driven bean.", null, null));
            }
        }
        return result;
    }
}
