package be.fgov.kszbcss.websphere.rhq;

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

import be.fgov.kszbcss.websphere.rhq.mbean.MBeanAttributeMatcherLocator;
import be.fgov.kszbcss.websphere.rhq.mbean.MBeanClient;
import be.fgov.kszbcss.websphere.rhq.support.configuration.ConfigurationFacetSupport;
import be.fgov.kszbcss.websphere.rhq.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.websphere.rhq.support.measurement.PMIMeasurementHandler;
import be.fgov.kszbcss.websphere.rhq.support.measurement.PMIModuleSelector;

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
        final MBeanClient mbean = server.getMBeanClient(new MBeanAttributeMatcherLocator(Utils.createObjectName("WebSphere:type=DataSource,*"), "jndiName", jndiName));
        this.mbean = mbean;
        measurementFacetSupport = new MeasurementFacetSupport(this);
        PMIModuleSelector moduleSelector = new PMIModuleSelector() {
            public String[] getPath() throws JMException, ConnectorException {
                // TODO: we should first try with the cached object name
                String providerName = mbean.getObjectName(true).getKeyProperty("JDBCProvider");
                return new String[] { PmiConstants.CONNPOOL_MODULE, providerName, jndiName };
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
