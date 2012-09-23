package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.Set;

import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;

public abstract class J2EEComponent<T extends ModuleComponent> extends WebSphereServiceComponent<T> {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<T> context = getResourceContext();
        ModuleComponent parent = context.getParentResourceComponent();
        WebSphereServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(this);
        // Applications may be installed with "Create MBeans for resources" disabled. In this case, there
        // is no MBean representing the bean/servlet. Therefore we always locate the PMI module starting from the
        // server.
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(),
                getPMIModule(), parent.getApplicationName() + "#" + parent.getModuleName(),
                getPMISubmodule(), context.getResourceKey()));
        PropertySimple suppressLogEventsProp = context.getPluginConfiguration().getSimple("suppressLogEvents");
        boolean suppressLogEvents = suppressLogEventsProp != null && Boolean.TRUE.equals(suppressLogEventsProp.getBooleanValue());
        context.getParentResourceComponent().registerLogEventContext(context.getResourceKey(), suppressLogEvents ? null : context.getEventContext());
    }
    
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public void stop() {
        ResourceContext<T> context = getResourceContext();
        context.getParentResourceComponent().unregisterLogEventContext(context.getResourceKey());
    }
    
    protected abstract String getPMIModule();
    protected abstract String getPMISubmodule();
}
