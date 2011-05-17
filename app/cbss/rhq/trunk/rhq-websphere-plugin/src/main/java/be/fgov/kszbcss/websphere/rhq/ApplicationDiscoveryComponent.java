package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.websphere.rhq.repository.ConfigDocument;

public class ApplicationDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    private static final Log log = LogFactory.getLog(ApplicationDiscoveryComponent.class);
    
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        WebSphereServer server = context.getParentResourceComponent().getServer();
        ConfigDocument serverIndex = server.getConfigDocument("cells/" + server.getCell() + "/nodes/" + server.getNode() + "/serverindex.xml");
//        log.info(IOUtils.toString(serverIndex.getContent(), "UTF-8"));
        for (ObjectName objectName : server.getAdminClient().queryNames(Utils.createObjectName("WebSphere:type=Application,*"), null)) {
            String applicationName = objectName.getKeyProperty("name");
            result.add(new DiscoveredResourceDetails(context.getResourceType(), applicationName, applicationName, null, "An enterprise application.", null, null));
        }
        return result;
    }
}
