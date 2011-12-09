package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.DeploymentConfigurationFacetSupport;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;

import com.ibm.websphere.pmi.PmiConstants;

public abstract class EnterpriseBeanComponent extends WebSphereServiceComponent<EJBModuleComponent> implements MeasurementFacet, ConfigurationFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    private DeploymentConfigurationFacetSupport configurationFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<EJBModuleComponent> context = getResourceContext();
        ModuleComponent parent = context.getParentResourceComponent();
        WebSphereServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(this);
//        MBeanClient mbean = server.getMBeanClient("WebSphere:type=" + getMBeanType() + ",Application=" + parent.getApplicationName() + ",EJBModule=" + parent.getModuleName() + ",name=" + context.getResourceKey() + ",*");
        // Applications may be installed with "Create MBeans for resources" disabled. In this case, there
        // is no MBean representing the bean. Therefore we always locate the PMI module starting from the
        // server.
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(),
                PmiConstants.BEAN_MODULE, parent.getApplicationName() + "#" + parent.getModuleName(),
                getPMISubmodule(), context.getResourceKey()));
        context.getParentResourceComponent().registerLogEventContext(context.getResourceKey(), context.getEventContext());
        configurationFacetSupport = new DeploymentConfigurationFacetSupport(getModule().getApplication(), getModuleName(), getBeanName());
    }
    
    protected abstract EnterpriseBeanType getType();
    
    // TODO: check if this is still needed
    protected abstract String getMBeanType();
    
    protected abstract String getPMISubmodule();
    
    public String getBeanName() {
        return getResourceContext().getResourceKey();
    }
    
    public EJBModuleComponent getModule() {
        return getResourceContext().getParentResourceComponent();
    }
    
    public String getModuleName() {
        return getModule().getModuleName();
    }
    
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
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

    public void stop() {
        ResourceContext<EJBModuleComponent> context = getResourceContext();
        context.getParentResourceComponent().unregisterLogEventContext(context.getResourceKey());
    }
}
