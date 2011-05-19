package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.websphere.rhq.mbean.MBean;

public class DynaCacheDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    private static final Log log = LogFactory.getLog(DynaCacheDiscoveryComponent.class); 
    
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        MBean mbean = new MBean(context.getParentResourceComponent().getServer(), Utils.createObjectName("WebSphere:type=DynaCache,*"));
        for (String instanceName : (String[])mbean.invoke("getCacheInstanceNames", new Object[0], new String[0])) {
            if (instanceName.equals("baseCache") || instanceName.startsWith("ws/")) {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring DynaCache " + instanceName);
                }
            } else {
                result.add(new DiscoveredResourceDetails(context.getResourceType(), instanceName, instanceName, null, instanceName, null, null));
            }
        }
        return result;
    }
}
