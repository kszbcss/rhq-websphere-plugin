package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.w3c.dom.Document;

import be.fgov.kszbcss.websphere.rhq.mbean.MBean;

import com.ibm.websphere.management.exception.ConnectorException;

public abstract class ModuleComponent extends WebSphereServiceComponent<ApplicationComponent> {
    private MBean mbean;
    private DeploymentDescriptorCache deploymentDescriptorCache;
    
    protected abstract String getMBeanType();
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<ApplicationComponent> context = getResourceContext();
        String applicationName = context.getParentResourceComponent().getApplicationName();
        String moduleName = context.getResourceKey();
        mbean = new MBean(getServer(), Utils.createObjectName("WebSphere:type=" + getMBeanType() + ",Application=" + applicationName + ",name=" + moduleName + ",*"));
        deploymentDescriptorCache = new DeploymentDescriptorCache(mbean);
    }

    public String getApplicationName() {
        return getResourceContext().getParentResourceComponent().getApplicationName();
    }
    
    public String getModuleName() {
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
    }
}
