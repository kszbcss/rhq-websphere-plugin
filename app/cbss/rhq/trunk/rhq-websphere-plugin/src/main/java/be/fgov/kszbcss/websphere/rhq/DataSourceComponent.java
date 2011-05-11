package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.StatDescriptor;
import com.ibm.websphere.pmi.stat.WSRangeStatistic;

public class DataSourceComponent extends PMIComponent<PMIComponent<?>> {
    private String jndiName;
    private MBean mbean;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        jndiName = getResourceContext().getResourceKey();
        mbean = new MBean(getServer(), new MBeanAttributeMatcherLocator(Utils.createObjectName("WebSphere:type=DataSource,*"), "jndiName", jndiName));
    }

    public void stop() {
    }

    public AvailabilityType getAvailability() {
        // TODO
        return AvailabilityType.UP;
    }

    @Override
    protected MBeanStatDescriptor getMBeanStatDescriptor() throws JMException, ConnectorException {
        String providerName = mbean.getObjectName().getKeyProperty("JDBCProvider");
        return new MBeanStatDescriptor(getServer().getServerMBean().getObjectName(), new StatDescriptor(new String[] { "connectionPoolModule", providerName, jndiName }));
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
