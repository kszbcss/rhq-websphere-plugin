package be.fgov.kszbcss.rhq.websphere;

import java.io.Serializable;

import javax.management.JMException;
import javax.management.ObjectName;

import org.rhq.core.pluginapi.event.EventContext;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;

public abstract class ApplicationServer extends WebSphereServer {
    public ApplicationServer(ProcessLocator processLocator) {
        super(processLocator);
    }

    public abstract <T extends Serializable> T queryConfig(ConfigQuery<T> query, boolean immediate) throws InterruptedException;

    public abstract String getClusterName() throws InterruptedException, JMException, ConnectorException;
    
    public abstract void registerStateChangeEventContext(ObjectName objectNamePattern, EventContext context);

    public abstract void unregisterStateChangeEventContext(ObjectName objectNamePattern);
}
