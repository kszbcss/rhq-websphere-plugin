package be.fgov.kszbcss.rhq.websphere.component.server;

import java.util.Collections;
import java.util.Set;

import javax.management.ObjectName;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ManualAddFacet;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.rhq.websphere.ConfigurationBasedProcessLocator;
import be.fgov.kszbcss.rhq.websphere.connector.SecureAdminClientProvider;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class WebSphereServerDiscoveryComponent implements ResourceDiscoveryComponent<ResourceComponent<?>>, ManualAddFacet<ResourceComponent<?>> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<ResourceComponent<?>> context) throws InvalidPluginConfigurationException, Exception {
        // We don't support automatic discovery (yet)
        return Collections.emptySet();
    }

    public DiscoveredResourceDetails discoverResource(Configuration pluginConfiguration, ResourceDiscoveryContext<ResourceComponent<?>> discoveryContext) throws InvalidPluginConfigurationException {
        ObjectName serverBeanName;
        try {
            AdminClient adminClient = new SecureAdminClientProvider(new ConfigurationBasedProcessLocator(pluginConfiguration)).createAdminClient();
            serverBeanName = adminClient.getServerMBean();
        } catch (ConnectorException ex) {
            throw new InvalidPluginConfigurationException("Unable to connect to server", ex);
        }
        String cell = serverBeanName.getKeyProperty("cell");
        String node = serverBeanName.getKeyProperty("node");
        String process = serverBeanName.getKeyProperty("process");
        String processType = serverBeanName.getKeyProperty("processType");
        boolean unmanaged;
        if (processType.equals("ManagedProcess")) {
            unmanaged = false;
        } else if (processType.equals("UnManagedProcess")) {
            unmanaged = true;
        } else {
            throw new InvalidPluginConfigurationException("Unsupported process type " + processType);
        }
        pluginConfiguration.getSimple("unmanaged").setBooleanValue(unmanaged);
        return new DiscoveredResourceDetails(discoveryContext.getResourceType(), cell + "/" + node + "/" + process,
                process, null, process + " (cell " + cell + ", node " + node + ")", pluginConfiguration, null);
    }
}
