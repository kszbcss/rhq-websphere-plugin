package be.fgov.kszbcss.rhq.websphere;

import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import com.ibm.websphere.management.AdminClient;

public class DataSourceDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        AdminClient adminClient = context.getParentResourceComponent().getServer().getAdminClient();
        for (ObjectName objectName : adminClient.queryNames(Utils.createObjectName("WebSphere:type=DataSource,*"), null)) {
            String jndiName = (String)adminClient.getAttribute(objectName, "jndiName");
            result.add(new DiscoveredResourceDetails(context.getResourceType(), jndiName, jndiName, null, "A data source", null, null));
        }
        return result;
    }
}
