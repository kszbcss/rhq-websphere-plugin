package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.rhq.websphere.component.jdbc.DataSourceComponent;

public class DB2MonitorDiscoveryComponent implements ResourceDiscoveryComponent<DataSourceComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<DataSourceComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        if (context.getParentResourceComponent().getConnectionFactoryInfo().getProperty("clientProgramName") != null) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), "default", "DB2 Monitor", null, "DB2 Monitor", null, null));
        }
        return result;
    }
}
