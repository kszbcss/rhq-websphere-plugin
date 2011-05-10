package be.fgov.kszbcss.websphere.rhq;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.rhq.core.pluginapi.inventory.ResourceContext;

public class ApplicationComponent extends StatsEnabledMBeanResourceComponent<WebSphereServerComponent> {
    private ObjectName objectNamePattern;
    
    @Override
    public void start(ResourceContext<WebSphereServerComponent> context) {
        super.start(context);
        try {
            objectNamePattern = new ObjectName("WebSphere:type=Application,name=" + context.getResourceKey() + ",*");
        } catch (MalformedObjectNameException ex) {
            throw new Error(ex);
        }
        context.getParentResourceComponent().getServer().registerStateChangeEventContext(objectNamePattern, context.getEventContext());
    }

    @Override
    public void stop() {
        getResourceContext().getParentResourceComponent().getServer().unregisterStateChangeEventContext(objectNamePattern);
        super.stop();
    }
}
