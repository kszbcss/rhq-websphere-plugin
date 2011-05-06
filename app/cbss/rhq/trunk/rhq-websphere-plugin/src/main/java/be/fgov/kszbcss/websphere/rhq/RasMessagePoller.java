package be.fgov.kszbcss.websphere.rhq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.rhq.core.domain.event.Event;
import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.pluginapi.event.EventPoller;

import com.ibm.websphere.ras.RasMessage;

public class RasMessagePoller implements EventPoller, NotificationListener {
    public static final String EVENT_TYPE = "RasMessage";
    
    private static final Map<String,EventSeverity> severityMap = new HashMap<String,EventSeverity>();
    
    static {
        severityMap.put(RasMessage.AUDIT, EventSeverity.INFO);
        severityMap.put(RasMessage.WARNING, EventSeverity.WARN);
        severityMap.put(RasMessage.ERROR, EventSeverity.ERROR);
        severityMap.put(RasMessage.FATAL, EventSeverity.FATAL);
    }
    
    private final List<Event> events = new ArrayList<Event>();
    
    public String getEventType() {
        return EVENT_TYPE;
    }

    public Set<Event> poll() {
        Set<Event> result = new HashSet<Event>();
        synchronized (events) {
            result.addAll(events);
            events.clear();
        }
        return result;
    }

    public void handleNotification(Notification notification, Object handback) {
        RasMessage rasMessage = (RasMessage)notification.getUserData();
        Event event = new Event(EVENT_TYPE, rasMessage.getMessageOriginator(), rasMessage.getTimeStamp(), severityMap.get(rasMessage.getMessageSeverity()), rasMessage.getLocalizedMessage(Locale.ENGLISH));
        synchronized (events) {
            events.add(event);
        }
    }
}
