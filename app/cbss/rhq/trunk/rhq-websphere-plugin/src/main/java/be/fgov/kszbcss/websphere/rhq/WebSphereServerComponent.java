package be.fgov.kszbcss.websphere.rhq;

import java.util.Set;

import javax.management.JMException;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.EmsException;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.event.Event;
import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.event.EventContext;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.websphere.rhq.connector.ems.WebsphereConnectionProvider;
import be.fgov.kszbcss.websphere.rhq.support.measurement.JMXAttributeGroupHandler;
import be.fgov.kszbcss.websphere.rhq.support.measurement.MeasurementFacetSupport;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.NotificationConstants;
import com.ibm.websphere.management.exception.ConnectorException;

public class WebSphereServerComponent implements WebSphereComponent<ResourceComponent<?>>, MeasurementFacet {
    private static final Log log = LogFactory.getLog(WebSphereServerComponent.class);
    
    private ResourceContext<ResourceComponent<?>> resourceContext;
    private WebSphereServer server;
    private EmsConnection connection;
    private MeasurementFacetSupport measurementFacetSupport;
    
    public void start(ResourceContext<ResourceComponent<?>> context) throws InvalidPluginConfigurationException, Exception {
        this.resourceContext = context;
        
        String[] parts = context.getResourceKey().split("/");
        
        server = new WebSphereServer(parts[0], parts[1], parts[2], context.getPluginConfiguration());
        server.init();
        
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.setDefaultHandler(new JMXAttributeGroupHandler(server.getServerMBean()));
        
        final EventContext eventContext = context.getEventContext();
        
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification notification, Object handback) {
                Utils.publishEvent(eventContext, new Event("ThreadMonitor", null, notification.getTimeStamp(), EventSeverity.INFO,
                        "source=" + notification.getSource() + "; type=" + notification.getType() + "; userData=" + notification.getUserData()));
            }
        };
        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType(NotificationConstants.TYPE_THREAD_MONITOR_THREAD_HUNG);
        filter.enableType(NotificationConstants.TYPE_THREAD_MONITOR_THREAD_CLEAR);
        server.addNotificationListener(new ObjectName("WebSphere:*"), listener, filter, null, true);
        
        filter = new NotificationFilterSupport();
        // TODO: use constants from NotificationConstants here
        filter.enableType("websphere.ras.audit");
        filter.enableType("websphere.ras.warning");
        filter.enableType("websphere.ras.error");
        filter.enableType("websphere.ras.fatal");
        server.addNotificationListener(new ObjectName("WebSphere:type=RasLoggingService,*"), new RasLoggingNotificationListener(eventContext), filter, null, true);
    }

    public ResourceContext<ResourceComponent<?>> getResourceContext() {
        return resourceContext;
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

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public void stop() {
        server.destroy();
        resourceContext.getEventContext().unregisterEventPoller(RasLoggingNotificationListener.EVENT_TYPE);
    }
}
