package be.fgov.kszbcss.websphere.rhq;

import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.plugins.jmx.JMXComponent;
import org.rhq.plugins.jmx.MBeanResourceDiscoveryComponent;

public class WebSphereMBeanResourceDiscoveryComponent<T extends JMXComponent> extends MBeanResourceDiscoveryComponent<JMXComponent> {
    @Override
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<JMXComponent> context) {
        // WebSphere MBean always have "unknown" properties; don't skip MBeans with these properties
        return discoverResources(context, false);
    }
}
