package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.EmsException;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class WebSphereServerComponent implements WebSphereComponent {
    private static final Log log = LogFactory.getLog(WebSphereServerComponent.class);
    
    private ResourceContext resourceContext;
    private WebSphereServer server;
    private EmsConnection connection;
    private RasMessagePoller poller;
    
    public void start(ResourceContext context) throws InvalidPluginConfigurationException, Exception {
        this.resourceContext = context;
        server = new WebSphereServer(context.getPluginConfiguration());
        poller = new RasMessagePoller(server);
        context.getEventContext().registerEventPoller(poller, 60);
    }

    public WebSphereServer getServer() {
        return server;
    }

    public synchronized EmsConnection getEmsConnection() {
        if (connection == null) {
            try {
                Configuration pluginConfig = resourceContext.getPluginConfiguration();
                ConnectionSettings connectionSettings = new ConnectionSettings();
                connectionSettings.setServerUrl(pluginConfig.getSimpleValue("host", null) + ":" + pluginConfig.getSimpleValue("port", null));
                ConnectionProvider connectionProvider = new WebsphereConnectionProvider(server.getAdminClient());
                // The connection settings are not required to establish the connection, but they
                // will still be used in logging:
                connectionProvider.initialize(connectionSettings);
                connection = connectionProvider.connect();
                
                // If this is not present, then EmbeddedJMXServerDiscoveryComponent will fail to
                // discover the platform MXBeans.
                connection.loadSynchronous(false);
                
            } catch (ConnectorException ex) {
                throw new EmsException(ex);
            }
        }
        return connection;
    }
    
    public AvailabilityType getAvailability() {
        AdminClient adminClient;
        try {
            adminClient = server.getAdminClient();
        } catch (ConnectorException ex) {
            log.debug("Unable to connect to server => server DOWN", ex);
            return AvailabilityType.DOWN;
        }
        ObjectName serverMBean;
        try {
            serverMBean = adminClient.getServerMBean();
        } catch (ConnectorException ex) {
            log.debug("Unable to get server MBean => server DOWN", ex);
            return AvailabilityType.DOWN;
        }
        String state;
        try {
            state = (String)adminClient.getAttribute(serverMBean, "state");
        } catch (ConnectorException ex) {
            log.debug("Failed to get 'state' attribute from the server MBean => server DOWN", ex);
            return AvailabilityType.DOWN;
        } catch (JMException ex) {
            log.warn("Unexpected management exception while getting the 'state' attribute from the server MBean", ex);
            return AvailabilityType.DOWN;
        }
        if (log.isDebugEnabled()) {
            log.debug("Server state = " + state);
        }
        if (state.equals("STARTED")) {
            return AvailabilityType.UP;
        } else {
            return AvailabilityType.DOWN;
        }
    }

    public void stop() {
        poller.unregisterListener();
        resourceContext.getEventContext().unregisterEventPoller(RasMessagePoller.EVENT_TYPE);
    }
}
