package be.fgov.kszbcss.websphere.rhq;

import java.util.Set;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.websphere.rhq.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.websphere.rhq.measurement.PMIMeasurementHandler;

public class ServletComponent extends WebSphereServiceComponent<WebModuleComponent> implements MeasurementFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<WebModuleComponent> context = getResourceContext();
        ModuleComponent parent = context.getParentResourceComponent();
        WebSphereServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(this);
        MBean mbean = new MBean(server, Utils.createObjectName("WebSphere:type=Servlet,Application=" + parent.getApplicationName() + ",WebModule=" + parent.getModuleName() + ",name=" + context.getResourceKey() + ",*"));
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(mbean));
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public AvailabilityType getAvailability() {
        // TODO Auto-generated method stub
        return AvailabilityType.UP;
    }

    public void stop() {
    }
}
