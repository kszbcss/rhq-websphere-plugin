package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.plugins.jmx.MBeanResourceComponent;

public class DataSourceDiscoveryComponent extends WebSphereMBeanResourceDiscoveryComponent<MBeanResourceComponent> {
    private static final Log log = LogFactory.getLog(DataSourceDiscoveryComponent.class);
    
    @Override
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<MBeanResourceComponent> context) {
        String[] dataSourceObjectNameStrings = (String[])context.getParentResourceComponent().getEmsBean().getAttribute("jdbcDataSources").getValue();
        Set<String> dataSourceKeys = new HashSet<String>();
        for (String s : dataSourceObjectNameStrings) {
            try {
                dataSourceKeys.add(new ObjectName(s).getKeyProperty("mbeanIdentifier"));
            } catch (MalformedObjectNameException ex) {
                log.error("Unable to parse object name string: " + s);
            }
        }
        Set<DiscoveredResourceDetails> result = super.discoverResources(context);
        for (Iterator<DiscoveredResourceDetails> it = result.iterator(); it.hasNext(); ) {
            if (!dataSourceKeys.contains(it.next().getResourceKey())) {
                it.remove();
            }
        }
        return result;
    }
}
