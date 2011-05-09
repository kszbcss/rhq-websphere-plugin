package be.fgov.kszbcss.websphere.rhq;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.event.Event;
import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.pluginapi.event.EventPoller;

import com.ibm.ejs.ras.RasMessageImpl2;
import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.ras.RasMessage;

public class RasMessagePoller implements EventPoller, NotificationListener {
    private static final Log log = LogFactory.getLog(RasMessagePoller.class);
    
    public static final String EVENT_TYPE = "RasMessage";
    
    private static final Map<String,EventSeverity> severityMap = new HashMap<String,EventSeverity>();
    private static final Field localizedMessageField;
    
    static {
        severityMap.put(RasMessage.AUDIT, EventSeverity.INFO);
        severityMap.put(RasMessage.WARNING, EventSeverity.WARN);
        severityMap.put(RasMessage.ERROR, EventSeverity.ERROR);
        severityMap.put(RasMessage.FATAL, EventSeverity.FATAL);
        try {
            localizedMessageField = RasMessageImpl2.class.getDeclaredField("ivLocalizedMessage");
        } catch (NoSuchFieldException ex) {
            throw new NoSuchFieldError(ex.getMessage());
        }
        localizedMessageField.setAccessible(true);
    }
    
    private final WebSphereServer server;
    private final List<Event> events = new ArrayList<Event>();
    private ObjectName rasLoggingService;
    private boolean listenerRegistered;
    
    public RasMessagePoller(WebSphereServer server) {
        this.server = server;
    }
    
    public String getEventType() {
        return EVENT_TYPE;
    }

    public Set<Event> poll() {
        if (listenerRegistered) {
            Set<Event> result = new HashSet<Event>();
            synchronized (events) {
                result.addAll(events);
                events.clear();
            }
            return result;
        } else {
            try {
                AdminClient adminClient = server.getAdminClient();
                rasLoggingService = adminClient.queryNames(new ObjectName("WebSphere:type=RasLoggingService,*"), null).iterator().next();
                NotificationFilterSupport filter = new NotificationFilterSupport();
                // TODO: use constants from NotificationConstants here
                filter.enableType("websphere.ras.audit");
                filter.enableType("websphere.ras.warning");
                filter.enableType("websphere.ras.error");
                filter.enableType("websphere.ras.fatal");
                // TODO: unregister the listeners somewhere
                adminClient.addNotificationListener(rasLoggingService, this, filter, null);
                listenerRegistered = true;
                log.info("Starting to receive logging events from " + rasLoggingService);
            } catch (ConnectorException ex) {
                log.error("Unable to register notification listener for RasLoggingService", ex);
            } catch (JMException ex) {
                log.error("Unable to register notification listener for RasLoggingService", ex);
            }
            return Collections.emptySet();
        }
    }

    public void handleNotification(Notification notification, Object handback) {
        RasMessage rasMessage = (RasMessage)notification.getUserData();
        // We extract the localized message using reflection because
        // getLocalizedMessage will always compare the locale. If there is a
        // locale mismatch and the necessary resource bundle is not found,
        // no message will be returned.
        String localizedMessage;
        try {
            localizedMessage = (String)localizedMessageField.get(rasMessage);
        } catch (IllegalAccessException ex) {
            throw new IllegalAccessError(ex.getMessage());
        }
        Event event = new Event(EVENT_TYPE, rasMessage.getMessageOriginator(), rasMessage.getTimeStamp(), severityMap.get(rasMessage.getMessageSeverity()), localizedMessage);
        synchronized (events) {
            events.add(event);
        }
    }
    
    public void unregisterListener() {
        if (listenerRegistered) {
            try {
                AdminClient adminClient = server.getAdminClient();
                adminClient.removeNotificationListener(rasLoggingService, this);
            } catch (ConnectorException ex) {
                // TODO: in this case we should try again later
                log.error("Unable to unregister notification listener for RasLoggingService", ex);
            } catch (JMException ex) {
                log.error("Unable to unregister notification listener for RasLoggingService", ex);
            }
        }
    }
}
