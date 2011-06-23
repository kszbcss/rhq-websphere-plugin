package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.util.Set;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;

public abstract class EnterpriseBeanComponent extends WebSphereServiceComponent<EJBModuleComponent> implements MeasurementFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<EJBModuleComponent> context = getResourceContext();
        ModuleComponent parent = context.getParentResourceComponent();
        WebSphereServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(this);
        MBeanClient mbean = server.getMBeanClient("WebSphere:type=" + getMBeanType() + ",Application=" + parent.getApplicationName() + ",EJBModule=" + parent.getModuleName() + ",name=" + context.getResourceKey() + ",*");
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(mbean));
        context.getParentResourceComponent().registerLogEventContext(context.getResourceKey(), context.getEventContext());
    }
    
    protected abstract String getMBeanType();
    
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public AvailabilityType getAvailability() {
        // TODO Auto-generated method stub
        return AvailabilityType.UP;
    }

    public void stop() {
        ResourceContext<EJBModuleComponent> context = getResourceContext();
        context.getParentResourceComponent().unregisterLogEventContext(context.getResourceKey());
    }
}
