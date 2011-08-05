package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.Set;

import javax.management.ObjectName;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.event.EventContext;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.support.measurement.JMXAttributeGroupHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;

public class ApplicationComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet {
    private ObjectName pattern;
    private MBeanClient mbean;
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() {
        ManagedServer server = getServer();
        ResourceContext<WebSphereServerComponent> context = getResourceContext();
        pattern = Utils.createObjectName("WebSphere:type=Application,name=" + context.getResourceKey() + ",*");
        mbean = server.getMBeanClient(pattern);
        server.registerStateChangeEventContext(pattern, context.getEventContext());
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.setDefaultHandler(new JMXAttributeGroupHandler(mbean));
    }
    
    public String getApplicationName() {
        return getResourceContext().getResourceKey();
    }
    
    public ApplicationInfo getApplicationInfo() {
        return getServer().queryConfig(new ApplicationInfoQuery(getApplicationName()));
    }
    
    public void registerLogEventContext(String moduleName, String componentName, EventContext context) {
        getResourceContext().getParentResourceComponent().registerLogEventContext(getApplicationName(), moduleName, componentName, context);
    }
    
    public void unregisterLogEventContext(String moduleName, String componentName) {
        getResourceContext().getParentResourceComponent().unregisterLogEventContext(getApplicationName(), moduleName, componentName);
    }
    
    public AvailabilityType getAvailability() {
        try {
            mbean.getAttribute("name");
            return AvailabilityType.UP;
        } catch (Exception ex) {
            return AvailabilityType.DOWN;
        }
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public void stop() {
        getResourceContext().getParentResourceComponent().getServer().unregisterStateChangeEventContext(pattern);
    }
}
