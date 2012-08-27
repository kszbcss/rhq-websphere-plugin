package be.fgov.kszbcss.rhq.websphere;

import java.io.Serializable;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilterSupport;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.event.EventContext;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.component.server.ClusterNameQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryService;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryServiceFactory;
import be.fgov.kszbcss.rhq.websphere.connector.notification.NotificationListenerRegistration;

public class ManagedServer extends ApplicationServer {
    private static final Log log = LogFactory.getLog(ManagedServer.class);
    
    private final StateChangeEventDispatcher stateEventDispatcher = new StateChangeEventDispatcher();
    private NotificationListenerRegistration stateEventListenerRegistration;
    private NodeAgent nodeAgent;
    private ConfigQueryService configQueryService;
    
    public ManagedServer(String cell, String node, String server, Configuration config) {
        super(cell, node, server, "ManagedProcess", new ConfigurationBasedProcessLocator(config));
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
            stateEventListenerRegistration = addNotificationListener(new ObjectName("WebSphere:*"), stateEventDispatcher, filter, null, true);
        } catch (MalformedObjectNameException ex) {
            log.error(ex);
        }
    }
    
    @Override
    public void destroy() {
        stateEventListenerRegistration.unregister();
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

    public synchronized NodeAgent getNodeAgent() throws ConnectorException {
        if (nodeAgent == null) {
            nodeAgent = new NodeAgent(getCell(), getNode(), new ParentProcessLocator(this));
        }
        return nodeAgent;
    }
    
    private synchronized ConfigQueryService getConfigQueryService() throws ConnectorException {
        if (configQueryService == null) {
            configQueryService = ConfigQueryServiceFactory.getInstance().getConfigQueryService(getCell(), getNodeAgent().getDeploymentManager());
        }
        return configQueryService;
    }

    public <T extends Serializable> T queryConfig(ConfigQuery<T> query, boolean immediate) throws InterruptedException, ConnectorException {
        return getConfigQueryService().query(query, immediate);
    }

    public String getClusterName() throws InterruptedException, JMException, ConnectorException {
        return queryConfig(new ClusterNameQuery(getNode(), getServer()), false);
    }
}
