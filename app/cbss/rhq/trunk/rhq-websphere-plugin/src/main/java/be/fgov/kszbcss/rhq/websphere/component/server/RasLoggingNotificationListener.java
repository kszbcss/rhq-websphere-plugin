package be.fgov.kszbcss.rhq.websphere.component.server;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.event.Event;
import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.Utils;

import com.ibm.ejs.ras.RasMessageImpl2;
import com.ibm.websphere.ras.RasMessage;

public class RasLoggingNotificationListener implements NotificationListener {
    public static final String EVENT_TYPE = "RasMessage";
    
    private static final Log log = LogFactory.getLog(RasLoggingNotificationListener.class);
    private static final Object lastSanitizeWarningLock = new Object();
    private static long lastSanitizeWarning;
    
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
    
    private static String sanitizeMessage(RasMessage message, String text) {
        StringBuilder buffer = null;
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            if (c < 32 && c != '\r' && c != '\n' && c != '\t') {
                if (buffer == null) {
                    buffer = new StringBuilder(text.length());
                    buffer.append(text, 0, i);
                }
                buffer.append(" ");
            } else if (buffer != null) {
                buffer.append(c);
            }
        }
        if (buffer == null) {
            return text;
        } else {
            String sanitizedText = buffer.toString();
            if (log.isWarnEnabled()) {
                // We only log a warning every 5 minutes
                synchronized (lastSanitizeWarningLock) {
                    long time = System.currentTimeMillis();
                    if (time - lastSanitizeWarning > 300000) {
                        lastSanitizeWarning = time;
                        log.warn("Got a RasMessage with invalid characters from " + message.getMessageOriginator() + ": " + sanitizedText);
                    }
                }
            }
            return sanitizedText;
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
        Utils.publishEvent(eventContext, new Event(EVENT_TYPE, rasMessage.getMessageOriginator(), rasMessage.getTimeStamp(),
                severityMap.get(rasMessage.getMessageSeverity()), sanitizeMessage(rasMessage, localizedMessage)));
    }
}
