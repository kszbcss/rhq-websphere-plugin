package be.fgov.kszbcss.rhq.websphere.component.xm4was;

import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import com.ibm.websphere.management.AdminClient;

import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;

public class OutboundConnectionCacheDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        AdminClient adminClient = context.getParentResourceComponent().getServer().getAdminClient();
        for (ObjectName objectName : adminClient.queryNames(Utils.createObjectName("WebSphere:type=XM4WAS.OutboundConnectionCache,*"), null)) {
            String name = objectName.getKeyProperty("name");
            result.add(new DiscoveredResourceDetails(context.getResourceType(), name, name, null, "Outbound HTTP connection cache for " + name, null, null));
        }
        return result;
    }
}
