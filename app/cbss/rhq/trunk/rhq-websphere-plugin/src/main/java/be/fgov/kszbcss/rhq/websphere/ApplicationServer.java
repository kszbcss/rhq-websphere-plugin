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
    private final String cell;
    private final String node;
    private final String server;
    
    public ApplicationServer(String cell, String node, String server, ProcessLocator processLocator) {
        super(processLocator);
        this.cell = cell;
        this.node = node;
        this.server = server;
    }

    public String getCell() {
        return cell;
    }

    public String getNode() {
        return node;
    }

    public String getServer() {
        return server;
    }

    public abstract <T extends Serializable> T queryConfig(ConfigQuery<T> query, boolean immediate) throws InterruptedException;

    public abstract String getClusterName() throws InterruptedException, JMException, ConnectorException;
    
    public abstract void registerStateChangeEventContext(ObjectName objectNamePattern, EventContext context);

    public abstract void unregisterStateChangeEventContext(ObjectName objectNamePattern);
}
