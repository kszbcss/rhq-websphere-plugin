package be.fgov.kszbcss.websphere.rhq;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;

import be.fgov.kszbcss.websphere.rhq.mbean.MBeanClient;

public abstract class ModuleComponent extends WebSphereServiceComponent<ApplicationComponent> {
    private MBeanClient mbean;
    
    protected abstract String getMBeanType();
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<ApplicationComponent> context = getResourceContext();
        String applicationName = context.getParentResourceComponent().getApplicationName();
        String moduleName = context.getResourceKey();
        mbean = getServer().getMBeanClient("WebSphere:type=" + getMBeanType() + ",Application=" + applicationName + ",name=" + moduleName + ",*");
    }

    public String getApplicationName() {
        return getResourceContext().getParentResourceComponent().getApplicationName();
    }
    
    public String getModuleName() {
        return getResourceContext().getResourceKey();
    }
    
    public ModuleInfo getModuleInfo() {
        return getResourceContext().getParentResourceComponent().getApplicationInfo().getModule(getModuleName());
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
