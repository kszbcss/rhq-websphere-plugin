package be.fgov.kszbcss.websphere.rhq;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.NotificationFilterSupport;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.plugins.jmx.JMXComponent;

public class WebSphereServerComponent implements JMXComponent {
    private static final Log log = LogFactory.getLog(WebSphereServerComponent.class);
    
    private ResourceContext resourceContext;
    private EmsConnection connection;
    private RasMessagePoller poller;
    
    public void start(ResourceContext context) throws InvalidPluginConfigurationException, Exception {
        this.resourceContext = context;
        poller = new RasMessagePoller();
        context.getEventContext().registerEventPoller(poller, 60);
    }

    public synchronized EmsConnection getEmsConnection() {
        if (connection == null) {
            connection = ConnectionHelper.createConnection(resourceContext.getPluginConfiguration());
            // TODO: Design: the notification support in EMS has some issues:
            //  1. Events are stored in memory (in addition to being dispatched to listeners), but there
            //     is no way to clear that list (see DNotification class). This would eventually lead to
            //     an OOM error.
            //  2. It is not possible to register a single listener for multiple notification types.
            //  3. There is a bug in DNotification: the filter is actually not passed to the addNotificationListener method.
            //  4. The notification filter used by EMS doesn't extend NotificationFilterSupport and is also
            //     not serializable. That means that it is evaluated locally (at best) instead of on the server.
            // TODO: Design: maybe we should turn things upside down and let the WebSphere plugin create the AdminClient
            // directly and only wrap it in an EMS object for compatibility with existing RHQ components
            try {
                ConnectionProvider provider = connection.getConnectionProvider();
                // TODO: quick and dirty hack; AbstractConnectionProvider is loaded from an isolated class loader
                MBeanServer mbs = (MBeanServer)provider.getClass().getMethod("getMBeanServer").invoke(provider);
                ObjectName rasLoggingService = (ObjectName)mbs.queryNames(new ObjectName("WebSphere:type=RasLoggingService,*"), null).iterator().next();
                NotificationFilterSupport filter = new NotificationFilterSupport();
                // TODO: use constants from NotificationConstants here
                filter.enableType("websphere.ras.audit");
                filter.enableType("websphere.ras.warning");
                filter.enableType("websphere.ras.error");
                filter.enableType("websphere.ras.fatal");
                // TODO: unregister the listeners somewhere
                mbs.addNotificationListener(rasLoggingService, poller, filter, null);
                log.info("Starting to receive logging events from " + rasLoggingService);
            } catch (JMException ex) {
                log.error("Unable to register notification listener for RasLoggingService", ex);
            } catch (IllegalAccessException ex) {
                log.error(ex);
            } catch (InvocationTargetException ex) {
                log.error(ex);
            } catch (NoSuchMethodException ex) {
                log.error(ex);
            }
        }
        return connection;
    }
    
    public AvailabilityType getAvailability() {
        List<EmsBean> serverBeans = getEmsConnection().queryBeans("WebSphere:type=Server,*");
        if (serverBeans.size() != 1) {
            return AvailabilityType.DOWN;
        } else {
            if (serverBeans.get(0).getOperation("getState").invoke().equals("STARTED")) {
                return AvailabilityType.UP;
            } else {
                return AvailabilityType.DOWN;
            }
        }
    }

    public void stop() {
        resourceContext.getEventContext().unregisterEventPoller(RasMessagePoller.EVENT_TYPE);
    }
}
