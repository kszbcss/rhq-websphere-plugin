package be.fgov.kszbcss.rhq.websphere;

import java.io.Serializable;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryService;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Represents a WebSphere application server. This can be a managed (federated) or unmanaged
 * (stand-alone) server.
 */
public abstract class ApplicationServer extends WebSphereServer {
    private ConfigQueryService configQueryService;
    
    public ApplicationServer(String cell, String node, String server, String processType, ProcessLocator processLocator) {
        super(cell, node, server, processType, processLocator);
    }

    @Override
    public void destroy() {
        if (configQueryService != null) {
            configQueryService.release();
            configQueryService = null;
        }
        super.destroy();
    }

    protected abstract ConfigQueryService createConfigQueryService() throws ConnectorException;
    
    public final <T extends Serializable> T queryConfig(ConfigQuery<T> query, boolean immediate) throws InterruptedException, ConnectorException {
        synchronized (this) {
            if (configQueryService == null) {
                configQueryService = createConfigQueryService();
            }
        }
        return configQueryService.query(query, immediate);
    }

    public abstract String getClusterName() throws InterruptedException, JMException, ConnectorException;
}
