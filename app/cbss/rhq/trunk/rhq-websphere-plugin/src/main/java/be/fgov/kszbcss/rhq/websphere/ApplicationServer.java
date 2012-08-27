package be.fgov.kszbcss.rhq.websphere;

import java.io.Serializable;

import javax.management.JMException;
import javax.management.ObjectName;

import org.rhq.core.pluginapi.event.EventContext;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;

/**
 * Represents a WebSphere application server. This can be a managed (federated) or unmanaged
 * (stand-alone) server.
 */
public abstract class ApplicationServer extends WebSphereServer {
    public ApplicationServer(String cell, String node, String server, String processType, ProcessLocator processLocator) {
        super(cell, node, server, processType, processLocator);
    }

    public abstract <T extends Serializable> T queryConfig(ConfigQuery<T> query, boolean immediate) throws InterruptedException, ConnectorException;

    public abstract String getClusterName() throws InterruptedException, JMException, ConnectorException;
    
    public abstract void registerStateChangeEventContext(ObjectName objectNamePattern, EventContext context);

    public abstract void unregisterStateChangeEventContext(ObjectName objectNamePattern);
}
