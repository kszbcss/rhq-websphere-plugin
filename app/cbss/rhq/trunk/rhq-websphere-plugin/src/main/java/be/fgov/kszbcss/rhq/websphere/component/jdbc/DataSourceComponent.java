package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;

import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryComponent;
import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryType;
import be.fgov.kszbcss.rhq.websphere.support.configuration.ConfigurationFacetSupport;

public class DataSourceComponent extends ConnectionFactoryComponent implements ConfigurationFacet {
    private static final Log log = LogFactory.getLog(DataSourceComponent.class);
    
    private ConfigurationFacetSupport configurationFacetSupport;
    
    @Override
    protected ConnectionFactoryType getType() {
        return ConnectionFactoryType.JDBC;
    }

    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        super.start();
        configurationFacetSupport = new ConfigurationFacetSupport(this, mbean);
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
