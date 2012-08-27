package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.Arrays;
import java.util.Set;

import javax.management.ObjectName;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.event.EventContext;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.proxy.ApplicationManager;
import be.fgov.kszbcss.rhq.websphere.support.measurement.JMXAttributeGroupHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;

public class ApplicationComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet {
    private ObjectName pattern;
    private MBeanClient mbean;
    private MeasurementFacetSupport measurementFacetSupport;
    private ApplicationManager applicationManager;
    
    @Override
    protected void start() {
        ApplicationServer server = getServer();
        ResourceContext<WebSphereServerComponent> context = getResourceContext();
        pattern = Utils.createObjectName("WebSphere:type=Application,name=" + context.getResourceKey() + ",*");
        mbean = server.getMBeanClient(pattern);
        server.registerStateChangeEventContext(pattern, context.getEventContext());
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("specVersion", new ApplicationSpecVersionMeasurementHandler(this));
        measurementFacetSupport.setDefaultHandler(new JMXAttributeGroupHandler(mbean));
        applicationManager = server.getMBeanClient("WebSphere:type=ApplicationManager,*").getProxy(ApplicationManager.class);
    }
    
    public String getApplicationName() {
        return getResourceContext().getResourceKey();
    }
    
    public ApplicationInfo getApplicationInfo(boolean immediate) throws InterruptedException, ConnectorException {
        return getServer().queryConfig(new ApplicationInfoQuery(getApplicationName()), immediate);
    }
    
    public ApplicationConfiguration getConfiguration(boolean immediate) throws InterruptedException, ConnectorException {
        return getServer().queryConfig(new ApplicationConfigurationQuery(getApplicationName()), immediate);
    }
    
    public void registerLogEventContext(String moduleName, EventContext context) {
        getResourceContext().getParentResourceComponent().registerLogEventContext(getApplicationName(), moduleName, null, context);
    }
    
    public void unregisterLogEventContext(String moduleName) {
        getResourceContext().getParentResourceComponent().unregisterLogEventContext(getApplicationName(), moduleName, null);
    }
    
    public void registerLogEventContext(String moduleName, String componentName, EventContext context) {
        getResourceContext().getParentResourceComponent().registerLogEventContext(getApplicationName(), moduleName, componentName, context);
    }
    
    public void unregisterLogEventContext(String moduleName, String componentName) {
        getResourceContext().getParentResourceComponent().unregisterLogEventContext(getApplicationName(), moduleName, componentName);
    }
    
    @Override
    protected boolean isConfigured(boolean immediate) throws Exception {
        ApplicationServer server = getServer();
        return Arrays.asList(server.queryConfig(new DeployedApplicationsQuery(server.getNode(), server.getServer()), immediate)).contains(getApplicationName());
    }

    protected AvailabilityType doGetAvailability() {
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

    protected OperationResult doInvokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("start")) {
            applicationManager.startApplication(getApplicationName());
        } else if (name.equals("stop")) {
            applicationManager.stopApplication(getApplicationName());
        }
        return null;
    }

    public void stop() {
        getResourceContext().getParentResourceComponent().getServer().unregisterStateChangeEventContext(pattern);
    }
}
