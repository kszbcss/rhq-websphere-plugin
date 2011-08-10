package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.util.Set;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import com.ibm.websphere.pmi.PmiConstants;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;
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
//        MBeanClient mbean = server.getMBeanClient("WebSphere:type=" + getMBeanType() + ",Application=" + parent.getApplicationName() + ",EJBModule=" + parent.getModuleName() + ",name=" + context.getResourceKey() + ",*");
        // Applications may be installed with "Create MBeans for resources" disabled. In this case, there
        // is no MBean representing the bean. Therefore we always locate the PMI module starting from the
        // server.
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(),
                PmiConstants.BEAN_MODULE, parent.getApplicationName() + "#" + parent.getModuleName(),
                getPMISubmodule(), context.getResourceKey()));
        context.getParentResourceComponent().registerLogEventContext(context.getResourceKey(), context.getEventContext());
    }
    
    // TODO: check if this is still needed
    protected abstract String getMBeanType();
    
    protected abstract String getPMISubmodule();
    
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
