package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import java.util.Set;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanLocator;
import be.fgov.kszbcss.rhq.websphere.mbean.ProcessInfo;
import be.fgov.kszbcss.rhq.websphere.support.configuration.ConfigurationFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIModuleSelector;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiConstants;
import com.ibm.websphere.pmi.stat.WSRangeStatistic;

public class DataSourceComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet, ConfigurationFacet {
    private static final Log log = LogFactory.getLog(DataSourceComponent.class);
    
    private String jndiName;
    private MBeanClient mbean;
    private MeasurementFacetSupport measurementFacetSupport;
    private ConfigurationFacetSupport configurationFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        final String jndiName = getResourceContext().getResourceKey();
        this.jndiName = jndiName;
        WebSphereServer server = getServer();
        final MBeanClient mbean = server.getMBeanClient(new MBeanLocator() {
            public Set<ObjectName> queryNames(ProcessInfo processInfo, AdminClient adminClient) throws JMException, ConnectorException {
                // TODO: we should have a specialized MBeanLocator implementation for this kind of dynamic lookup
                DataSourceInfo dataSource = getDataSourceInfo();
                return adminClient.queryNames(new ObjectName("WebSphere:type=DataSource,name=" + dataSource.getDataSourceName() + ",JDBCProvider=" + dataSource.getProviderName() + ",*"), null);
            }
        });
        this.mbean = mbean;
        measurementFacetSupport = new MeasurementFacetSupport(this);
        PMIModuleSelector moduleSelector = new PMIModuleSelector() {
            public String[] getPath() throws JMException, ConnectorException {
                DataSourceInfo dataSource = getDataSourceInfo();
                return new String[] { PmiConstants.CONNPOOL_MODULE, dataSource.getProviderName(), dataSource.getJndiName() };
            }
        };
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(), moduleSelector) {
            @Override
            protected double getValue(String name, WSRangeStatistic statistic) {
                if (name.equals("PercentMaxed") || name.equals("PercentUsed")) {
                    return ((double)statistic.getCurrent())/100;
                } else {
                    return super.getValue(name, statistic);
                }
            }
        });
        configurationFacetSupport = new ConfigurationFacetSupport(this, mbean);
    }
    
    DataSourceInfo getDataSourceInfo() throws JMException, ConnectorException {
        ManagedServer server = getServer();
        for (DataSourceInfo dataSource : server.queryConfig(new DataSourceQuery(server.getNode(), server.getServer()))) {
            if (dataSource.getJndiName().equals(jndiName)) {
                return dataSource;
            }
        }
        throw new RuntimeException("Configuration object for " + jndiName + " not found");
    }
    
    public void stop() {
    }

    public AvailabilityType getAvailability() {
        // TODO
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public Configuration loadResourceConfiguration() throws Exception {
        // Data source are initialized lazily upon first lookup. If a data source has not been initialized yet,
        // some configuration attributes are unavailable. This state can be detected by checking the dataSourceName
        // attribute.
        if (mbean.getAttribute("dataSourceName") == null) {
            if (log.isDebugEnabled()) {
                log.debug("Data source " + jndiName + " is not initialized; configuration will not be loaded");
            }
            // There seems to be no way to let the plugin container know that the configuration
            // is not available. But the plugin container code does a null check (and throws an exception).
            return null;
        } else {
            return configurationFacetSupport.loadResourceConfiguration();
        }
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
        configurationFacetSupport.updateResourceConfiguration(report);
    }
}
