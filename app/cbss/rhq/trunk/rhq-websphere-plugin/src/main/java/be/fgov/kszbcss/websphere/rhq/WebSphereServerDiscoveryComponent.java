package be.fgov.kszbcss.websphere.rhq;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.EmsBeanName;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ManualAddFacet;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public class WebSphereServerDiscoveryComponent implements ResourceDiscoveryComponent, ManualAddFacet {
    public Set discoverResources(ResourceDiscoveryContext context) throws InvalidPluginConfigurationException, Exception {
        // We don't support automatic discovery (yet)
        return Collections.emptySet();
    }

    public DiscoveredResourceDetails discoverResource(Configuration pluginConfiguration, ResourceDiscoveryContext discoveryContext) throws InvalidPluginConfigurationException {
        EmsConnection connection = ConnectionHelper.createConnection(pluginConfiguration);
        List<EmsBean> servers = connection.queryBeans("WebSphere:type=Server,*");
        if (servers.size() > 1) {
            throw new InvalidPluginConfigurationException("Found more than one Server MBean. Are you connecting to an administrative agent?");
        }
        EmsBeanName serverBeanName = servers.get(0).getBeanName();
        String cell = serverBeanName.getKeyProperty("cell");
        String node = serverBeanName.getKeyProperty("node");
        String process = serverBeanName.getKeyProperty("process");
        return new DiscoveredResourceDetails(discoveryContext.getResourceType(), cell + "/" + node + "/" + process,
                process, null, process + " (cell " + cell + ", node " + node + ")", pluginConfiguration, null);
    }
}
