package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;
import javax.management.ObjectName;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.StatDescriptor;
import com.ibm.websphere.pmi.stat.WSRangeStatistic;

public class DataSourceComponent extends PMIComponent<PMIComponent<?>> {
    private MBean dataSourceMBean;
    private MBean providerMBean;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
    }

    public void stop() {
    }

    public AvailabilityType getAvailability() {
        // TODO
        return AvailabilityType.UP;
    }

    private void loadMBeans() throws JMException, ConnectorException {
        String jndiName = getResourceContext().getResourceKey();
        WebSphereServer server = getServer();
        AdminClient adminClient = server.getAdminClient();
        for (ObjectName objectName : adminClient.queryNames(Utils.createObjectName("WebSphere:type=DataSource,*"), null)) {
            if (adminClient.getAttribute(objectName, "jndiName").equals(jndiName)) {
                // TODO: do this properly
                dataSourceMBean = new MBean(server, objectName);
                providerMBean = new MBean(server, adminClient.queryNames(Utils.createObjectName("WebSphere:type=JDBDProvider,name=" + objectName.getKeyProperty("JDBDProvider") + ",*"), null).iterator().next());
            }
        }
    }
    
    @Override
    protected MBeanStatDescriptor getMBeanStatDescriptor() throws JMException, ConnectorException {
        if (dataSourceMBean == null) {
            loadMBeans();
        }
        return new MBeanStatDescriptor(providerMBean.getObjectName(), new StatDescriptor(new String[] { getResourceContext().getResourceKey() }));
    }

    @Override
    protected double getValue(String name, WSRangeStatistic statistic) {
        if (name.equals("PercentMaxed")) {
            return ((double)statistic.getCurrent())/100;
        } else {
            return super.getValue(name, statistic);
        }
    }
}
