package be.fgov.kszbcss.rhq.websphere;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.event.Event;
import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.pluginapi.event.EventContext;

public class StateChangeEventDispatcher implements NotificationListener {
    private static final Log log = LogFactory.getLog(StateChangeEventDispatcher.class);
    
    private final Map<ObjectName,EventContext> contextMap = Collections.synchronizedMap(new HashMap<ObjectName,EventContext>());
    
    public void registerEventContext(ObjectName objectNamePattern, EventContext context) {
        contextMap.put(objectNamePattern, context);
    }
    
    public void unregisterEventContext(ObjectName objectNamePattern) {
        contextMap.remove(objectNamePattern);
    }

    public void handleNotification(Notification notification, Object handback) {
        ObjectName source = (ObjectName)notification.getSource();
        EventContext context = null;
        for (Map.Entry<ObjectName,EventContext> entry : contextMap.entrySet()) {
            if (entry.getKey().apply(source)) {
                context = entry.getValue();
            }
        }
        if (context == null) {
            log.warn("Got a state change event for which no EventContext has been registered; type=" + notification.getType() + "; source = " + notification.getSource());
        } else {
            Utils.publishEvent(context, new Event("StateChange", notification.getType(), notification.getTimeStamp(), EventSeverity.INFO, notification.getMessage()));
        }
    }
}
