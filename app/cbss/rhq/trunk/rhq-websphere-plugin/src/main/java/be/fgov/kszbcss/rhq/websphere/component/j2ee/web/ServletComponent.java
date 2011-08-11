package be.fgov.kszbcss.rhq.websphere.component.j2ee.web;

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
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;

import com.ibm.websphere.pmi.PmiConstants;

public class ServletComponent extends WebSphereServiceComponent<WebModuleComponent> implements MeasurementFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<WebModuleComponent> context = getResourceContext();
        ModuleComponent parent = context.getParentResourceComponent();
        WebSphereServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(this);
//        MBeanClient mbean = server.getMBeanClient("WebSphere:type=Servlet,Application=" + parent.getApplicationName() + ",WebModule=" + parent.getModuleName() + ",name=" + context.getResourceKey() + ",*");
        // Applications may be installed with "Create MBeans for resources" disabled. In this case, there
        // is no MBean representing the servlet. Therefore we always locate the PMI module starting from the
        // server.
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(),
                PmiConstants.WEBAPP_MODULE, parent.getApplicationName() + "#" + parent.getModuleName(),
                PmiConstants.SERVLET_SUBMODULE, context.getResourceKey()));
        context.getParentResourceComponent().registerLogEventContext(context.getResourceKey(), context.getEventContext());
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public AvailabilityType getAvailability() {
        // The MBean representing the servlet is registered lazily (or not at all if the application is
        // configured with "Create MBeans for resources" disabled). Therefore the only check we can do is
        // to see if the servlet is declared in the deployment descriptor. This is important so that we
        // can identify servlets that no longer exist.
        try {
            ResourceContext<WebModuleComponent> context = getResourceContext();
            return context.getParentResourceComponent().getServletNames().contains(context.getResourceKey()) ? AvailabilityType.UP : AvailabilityType.DOWN;
        } catch (Exception ex) {
            return AvailabilityType.DOWN;
        }
    }

    public void stop() {
        ResourceContext<WebModuleComponent> context = getResourceContext();
        context.getParentResourceComponent().unregisterLogEventContext(context.getResourceKey());
    }
}
