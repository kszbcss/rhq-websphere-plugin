package be.fgov.kszbcss.rhq.websphere.component;

import org.mc4j.ems.connection.EmsConnection;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;

public abstract class WebSphereServiceComponent<T extends WebSphereComponent<?>> implements WebSphereComponent<T> {
    private ResourceContext<T> context;

    public final void start(ResourceContext<T> context) throws InvalidPluginConfigurationException, Exception {
        this.context = context;
        start();
    }
    
    public final ResourceContext<T> getResourceContext() {
        return context;
    }

    protected abstract void start() throws InvalidPluginConfigurationException, Exception;
    
    public final EmsConnection getEmsConnection() {
        return context.getParentResourceComponent().getEmsConnection();
    }

    public final ManagedServer getServer() {
        return context.getParentResourceComponent().getServer();
    }
}
