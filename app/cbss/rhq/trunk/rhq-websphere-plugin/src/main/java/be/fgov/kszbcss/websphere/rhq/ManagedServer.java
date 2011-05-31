package be.fgov.kszbcss.websphere.rhq;

import java.io.Serializable;

import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilterSupport;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.websphere.rhq.config.ConfigQuery;
import be.fgov.kszbcss.websphere.rhq.config.ConfigQueryService;
import be.fgov.kszbcss.websphere.rhq.config.ConfigQueryServiceFactory;

public class ManagedServer extends WebSphereServer {
    private static final Log log = LogFactory.getLog(ManagedServer.class);
    
    private final String cell;
    private final StateChangeEventDispatcher stateEventDispatcher = new StateChangeEventDispatcher();
    private NodeAgent nodeAgent;
    private ConfigQueryService configQueryService;
    
    public ManagedServer(String cell, String node, String server, Configuration config) {
        super(/*cell, node, server*/ new ConfigurationBasedProcessLocator(config)); // TODO: use this info to check we are connecting to the right server
        this.cell = cell;
    }

    @Override
    public void init() {
        super.init();
        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType("j2ee.state.starting");
        filter.enableType("j2ee.state.running");
        filter.enableType("j2ee.state.stopping");
        filter.enableType("j2ee.state.stopped");
        filter.enableType("j2ee.state.failed");
        try {
            addNotificationListener(new ObjectName("WebSphere:*"), stateEventDispatcher, filter, null, true);
        } catch (MalformedObjectNameException ex) {
            log.error(ex);
        }
    }
    
    @Override
    public void destroy() {
        if (configQueryService != null) {
            configQueryService.release();
            configQueryService = null;
        }
        super.destroy();
    }

    public void registerStateChangeEventContext(ObjectName objectNamePattern, EventContext context) {
        stateEventDispatcher.registerEventContext(objectNamePattern, context);
    }

    public void unregisterStateChangeEventContext(ObjectName objectNamePattern) {
        stateEventDispatcher.unregisterEventContext(objectNamePattern);
    }

    public synchronized NodeAgent getNodeAgent() {
        if (nodeAgent == null) {
            nodeAgent = new NodeAgent(new ParentProcessLocator(this));
        }
        return nodeAgent;
    }
    
    private synchronized ConfigQueryService getConfigQueryService() {
        if (configQueryService == null) {
            configQueryService = ConfigQueryServiceFactory.getInstance().getConfigQueryService(cell, getNodeAgent().getDeploymentManager());
        }
        return configQueryService;
    }

    public <T extends Serializable> T queryConfig(ConfigQuery<T> query) {
        return getConfigQueryService().query(query);
    }
}
