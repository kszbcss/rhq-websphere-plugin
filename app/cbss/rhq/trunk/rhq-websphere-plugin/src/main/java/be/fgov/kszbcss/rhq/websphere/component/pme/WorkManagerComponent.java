package be.fgov.kszbcss.rhq.websphere.component.pme;

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

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.ThreadPoolPMIMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;
import be.fgov.kszbcss.rhq.websphere.support.configuration.ConfigurationFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;

public class WorkManagerComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet, ConfigurationFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    private ConfigurationFacetSupport configurationFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<WebSphereServerComponent> context = getResourceContext();
        measurementFacetSupport = new MeasurementFacetSupport(this);
        ManagedServer server = getServer();
        String jndiName = context.getResourceKey();
        measurementFacetSupport.addHandler("stats", new ThreadPoolPMIMeasurementHandler(server.getServerMBean(),
                new WorkManagerThreadPoolPMIModuleSelector(server, jndiName)));
        configurationFacetSupport = new ConfigurationFacetSupport(this,
                server.getMBeanClient(new WorkManagerThreadPoolMBeanLocator(jndiName)), true);
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public Configuration loadResourceConfiguration() throws Exception {
        return configurationFacetSupport.loadResourceConfiguration();
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
        configurationFacetSupport.updateResourceConfiguration(report);
    }

    @Override
    protected boolean isConfigured(boolean immediate) throws Exception {
        ManagedServer server = getServer();
        return server.queryConfig(new WorkManagerMapQuery(server.getNode(), server.getServer()), immediate).containsKey(getResourceContext().getResourceKey());
    }

    protected AvailabilityType doGetAvailability() {
        // TODO Auto-generated method stub
        return AvailabilityType.UP;
    }

    public void stop() {
    }
}
