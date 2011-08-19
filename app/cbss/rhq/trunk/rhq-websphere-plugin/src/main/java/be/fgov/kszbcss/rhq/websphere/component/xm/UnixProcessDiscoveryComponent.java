package be.fgov.kszbcss.rhq.websphere.component.xm;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;

import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.StatDescriptor;
import com.ibm.websphere.pmi.stat.WSStats;

public class UnixProcessDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    private static final Log log = LogFactory.getLog(UnixProcessDiscoveryComponent.class);
    
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        WebSphereServer server = context.getParentResourceComponent().getServer();
        MBeanStatDescriptor descriptor = new MBeanStatDescriptor(server.getServerMBean().getObjectName(true), new StatDescriptor(new String[] { "ProcStats" }));
        WSStats stats;
        try {
            stats = server.getWSStats(descriptor);
        } catch (Exception ex) {
            log.warn("Stats lookup failed", ex);
            stats = null;
        }
        if (stats == null) {
            log.debug("ProcStats PMI module not available");
        } else {
            log.debug("Discovered ProcStats PMI module");
            result.add(new DiscoveredResourceDetails(context.getResourceType(), "default", "UNIX Process", null, "UNIX Process Statistics", null, null));
        }
        return result;
    }
}
