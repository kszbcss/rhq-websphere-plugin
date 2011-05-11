package be.fgov.kszbcss.websphere.rhq;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.rhq.core.domain.event.Event;
import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.pluginapi.event.EventContext;

import com.ibm.ejs.ras.RasMessageImpl2;
import com.ibm.websphere.ras.RasMessage;

public class RasLoggingNotificationListener implements NotificationListener {
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
    
    private final EventContext eventContext;
    
    public RasLoggingNotificationListener(EventContext eventContext) {
        this.eventContext = eventContext;
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
        eventContext.publishEvent(new Event(EVENT_TYPE, rasMessage.getMessageOriginator(), rasMessage.getTimeStamp(), severityMap.get(rasMessage.getMessageSeverity()), localizedMessage));
    }
}
