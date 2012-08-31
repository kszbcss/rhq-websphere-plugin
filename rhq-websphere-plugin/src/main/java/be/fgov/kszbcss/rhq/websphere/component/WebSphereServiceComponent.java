package be.fgov.kszbcss.rhq.websphere.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;

public abstract class WebSphereServiceComponent<T extends WebSphereComponent<?>> implements WebSphereComponent<T>, OperationFacet {
    private static final Log log = LogFactory.getLog(WebSphereServiceComponent.class);
    
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

    public final ApplicationServer getServer() {
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
    protected abstract boolean isConfigured(boolean immediate) throws Exception;

    public final AvailabilityType getAvailability() {
        try {
            if (!isConfigured(false)) {
                log.debug("isConfigured=false => availability == DOWN");
                return AvailabilityType.DOWN;
            } else {
                log.debug("isConfigured=true; continue with checking the runtime state");
            }
        } catch (Exception ex) {
            log.debug("Caught exception thrown by isConfigured => availability == DOWN", ex);
            return AvailabilityType.DOWN;
        }
        return doGetAvailability();
    }
    
    protected abstract AvailabilityType doGetAvailability();

    public final OperationResult invokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("checkConfiguration")) {
            // TODO: This doesn't have the expected effect - in the GUI the following error message is shown: "There was an error looking up this operation's results."
            //       Also, the result is not saved in the database.
            OperationResult result = new OperationResult();
            Configuration results = result.getComplexResults();
            results.put(new PropertySimple("isConfigured", Boolean.valueOf(isConfigured(true))));
            return result;
        } else {
            return doInvokeOperation(name, parameters);
        }
    }
    
    protected OperationResult doInvokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        return null;
    }
}
