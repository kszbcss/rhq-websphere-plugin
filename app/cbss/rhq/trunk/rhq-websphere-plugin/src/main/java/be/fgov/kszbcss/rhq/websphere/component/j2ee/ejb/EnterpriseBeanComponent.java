package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.component.j2ee.DeploymentConfigurationFacetSupport;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.J2EEComponent;

import com.ibm.websphere.pmi.PmiConstants;

public abstract class EnterpriseBeanComponent extends J2EEComponent<EJBModuleComponent> implements MeasurementFacet, ConfigurationFacet {
    private DeploymentConfigurationFacetSupport configurationFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        super.start();
        configurationFacetSupport = new DeploymentConfigurationFacetSupport(getModule().getApplication(), getModuleName(), getBeanName());
    }
    
    protected abstract EnterpriseBeanType getType();
    
    @Override
    protected final String getPMIModule() {
        return PmiConstants.BEAN_MODULE;
    }

    public String getBeanName() {
        return getResourceContext().getResourceKey();
    }
    
    public EJBModuleComponent getModule() {
        return getResourceContext().getParentResourceComponent();
    }
    
    public String getModuleName() {
        return getModule().getModuleName();
    }
    
    @Override
    protected boolean isConfigured() throws Exception {
        return getModule().getBeanNames(getType()).contains(getBeanName());
    }

    protected AvailabilityType doGetAvailability() {
        // Same as for servlets: if the bean is configured, then it is expected to be available.
        return AvailabilityType.UP;
    }

    public final Configuration loadResourceConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        loadResourceConfiguration(configuration);
        return configuration;
    }
    
    // May be overridden in subclasses to add configurations specific to bean types
    protected void loadResourceConfiguration(Configuration configuration) throws Exception {
        configurationFacetSupport.loadResourceConfiguration(configuration);
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
    }
}
