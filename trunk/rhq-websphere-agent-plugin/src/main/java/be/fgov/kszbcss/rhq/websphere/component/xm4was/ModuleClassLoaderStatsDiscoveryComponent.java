package be.fgov.kszbcss.rhq.websphere.component.xm4was;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;

import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.StatDescriptor;
import com.ibm.websphere.pmi.stat.WSStats;

public class ModuleClassLoaderStatsDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServiceComponent<?>> {
    private static final Log log = LogFactory.getLog(ModuleClassLoaderStatsDiscoveryComponent.class);
    
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServiceComponent<?>> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        WebSphereServer server = context.getParentResourceComponent().getServer();
        MBeanStatDescriptor descriptor = new MBeanStatDescriptor(server.getServerMBean().getObjectName(true), new StatDescriptor(new String[] { "ClassLoaderStats" }));
        WSStats stats;
        try {
            stats = server.getWSStats(descriptor);
        } catch (Exception ex) {
            log.warn("Stats lookup failed", ex);
            stats = null;
        }
        if (stats == null) {
            log.debug("XM4WAS class loader monitor PMI module not found");
        } else {
            log.debug("XM4WAS class loader monitor PMI module found");
            result.add(new DiscoveredResourceDetails(context.getResourceType(), "default", "Class Loader Statistics", null, "Class Loader Stats", null, null));
        }
        return result;
    }
}
