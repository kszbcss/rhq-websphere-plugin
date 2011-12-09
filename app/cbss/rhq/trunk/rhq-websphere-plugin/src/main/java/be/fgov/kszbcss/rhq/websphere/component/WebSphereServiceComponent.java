package be.fgov.kszbcss.rhq.websphere.component;

import org.mc4j.ems.connection.EmsConnection;
import org.rhq.core.domain.measurement.AvailabilityType;
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
    
    /**
     * Determine whether the resource corresponding to this component is still present in the
     * WebSphere configuration.
     * 
     * @return <code>true</code> if the resource is present in the WebSphere configuration,
     *         <code>false</code> otherwise
     * @throws Exception 
     */
    protected abstract boolean isConfigured() throws Exception;

    public final AvailabilityType getAvailability() {
        try {
            if (!isConfigured()) {
                return AvailabilityType.DOWN;
            }
        } catch (Exception ex) {
            return AvailabilityType.DOWN;
        }
        return doGetAvailability();
    }
    
    protected abstract AvailabilityType doGetAvailability();
}
