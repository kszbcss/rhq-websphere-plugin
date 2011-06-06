package be.fgov.kszbcss.rhq.websphere;

import java.util.Collections;
import java.util.Set;

import javax.management.ObjectName;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ManualAddFacet;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class WebSphereServerDiscoveryComponent implements ResourceDiscoveryComponent, ManualAddFacet {
    public Set discoverResources(ResourceDiscoveryContext context) throws InvalidPluginConfigurationException, Exception {
        // We don't support automatic discovery (yet)
        return Collections.emptySet();
    }

    public DiscoveredResourceDetails discoverResource(Configuration pluginConfiguration, ResourceDiscoveryContext discoveryContext) throws InvalidPluginConfigurationException {
        try {
            AdminClient adminClient = ConnectionHelper.createAdminClient(pluginConfiguration);
            ObjectName serverBeanName = adminClient.getServerMBean();
            String cell = serverBeanName.getKeyProperty("cell");
            String node = serverBeanName.getKeyProperty("node");
            String process = serverBeanName.getKeyProperty("process");
            return new DiscoveredResourceDetails(discoveryContext.getResourceType(), cell + "/" + node + "/" + process,
                    process, null, process + " (cell " + cell + ", node " + node + ")", pluginConfiguration, null);
        } catch (ConnectorException ex) {
            throw new InvalidPluginConfigurationException("Unable to connect to server", ex);
        }
    }
}
