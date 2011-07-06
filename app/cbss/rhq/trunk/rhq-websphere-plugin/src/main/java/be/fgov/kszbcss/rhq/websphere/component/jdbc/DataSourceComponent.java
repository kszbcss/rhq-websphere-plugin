package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import java.util.Set;

import javax.management.JMException;

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
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.support.configuration.ConfigurationFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIModuleSelector;

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
        jndiName = getResourceContext().getResourceKey();
        final ManagedServer server = getServer();
        mbean = server.getMBeanClient(new DataSourceMBeanLocator(jndiName));
        measurementFacetSupport = new MeasurementFacetSupport(this);
        PMIModuleSelector moduleSelector = new PMIModuleSelector() {
            public String[] getPath() throws JMException, ConnectorException {
                DataSourceInfo cf = server.queryConfig(new DataSourceQuery(server.getNode(), server.getServer())).getByJndiName(jndiName);
                return new String[] { PmiConstants.CONNPOOL_MODULE, cf.getProviderName(), cf.getJndiName() };
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
    
    public void stop() {
    }

    public DataSourceInfo getDataSourceInfo() throws JMException, ConnectorException {
        ManagedServer server = getServer();
        return server.queryConfig(new DataSourceQuery(server.getNode(), server.getServer())).getByJndiName(jndiName);
    }
    
    public AvailabilityType getAvailability() {
        try {
            mbean.getObjectName(true);
            return AvailabilityType.UP;
        } catch (Exception ex) {
            return AvailabilityType.DOWN;
        }
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
