package be.fgov.kszbcss.rhq.websphere.component.xm;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import com.ibm.websphere.pmi.PmiModuleConfig;

import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;

public class UnixProcessDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        PmiModuleConfig config = context.getParentResourceComponent().getServer().getPerf().getConfig("be.fgov.kszbcss.rhq.websphere.xm.ProcStats");
        if (config != null && config.getNumData() > 0) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), "default", "UNIX Process", null, "UNIX Process Statistics", null, null));
        }
        return result;
    }
}
