package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.event.EventContext;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;

import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;

public abstract class ModuleComponent extends WebSphereServiceComponent<ApplicationComponent> {
    private MBeanClient mbean;
    
    protected abstract String getMBeanType();
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        mbean = getServer().getMBeanClient("WebSphere:type=" + getMBeanType() + ",Application=" + getApplicationName() + ",name=" + getModuleName() + ",*");
    }

    public ApplicationComponent getApplication() {
        return getResourceContext().getParentResourceComponent();
    }
    
    public String getApplicationName() {
        return getApplication().getApplicationName();
    }
    
    public String getModuleName() {
        return getResourceContext().getResourceKey();
    }
    
    public void registerLogEventContext(String componentName, EventContext context) {
        getApplication().registerLogEventContext(getModuleName(), componentName, context);
    }
    
    public void unregisterLogEventContext(String componentName) {
        getApplication().unregisterLogEventContext(getModuleName(), componentName);
    }
    
    public ModuleInfo getModuleInfo() {
        return getApplication().getApplicationInfo().getModule(getModuleName());
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
