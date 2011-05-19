package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;
import javax.management.ObjectName;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.w3c.dom.Document;

import be.fgov.kszbcss.websphere.rhq.mbean.MBeanClient;

import com.ibm.websphere.management.exception.ConnectorException;

public class ApplicationComponent extends WebSphereServiceComponent<WebSphereServerComponent> {
    private ObjectName pattern;
    private MBeanClient mbean;
    private DeploymentDescriptorCache deploymentDescriptorCache;
    
    @Override
    protected void start() {
        WebSphereServer server = getServer();
        ResourceContext<WebSphereServerComponent> context = getResourceContext();
        pattern = Utils.createObjectName("WebSphere:type=Application,name=" + context.getResourceKey() + ",*");
        mbean = server.getMBeanClient(pattern);
        server.registerStateChangeEventContext(pattern, context.getEventContext());
        deploymentDescriptorCache = new DeploymentDescriptorCache(mbean);
    }
    
    public String getApplicationName() {
        return getResourceContext().getResourceKey();
    }
    
    public Document getDeploymentDescriptor() throws JMException, ConnectorException {
        return deploymentDescriptorCache.getContent();
    }
    
    public AvailabilityType getAvailability() {
        try {
            mbean.getAttribute("name");
            return AvailabilityType.UP;
        } catch (Exception ex) {
            return AvailabilityType.DOWN;
        }
    }

    public void stop() {
        getResourceContext().getParentResourceComponent().getServer().unregisterStateChangeEventContext(pattern);
    }
}
