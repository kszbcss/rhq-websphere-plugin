package be.fgov.kszbcss.websphere.rhq;

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
    
    public void registerEventContext(ObjectName bean, EventContext context) {
        contextMap.put(bean, context);
    }
    
    public void unregisterEventContext(ObjectName bean) {
        contextMap.remove(bean);
    }

    public void handleNotification(Notification notification, Object handback) {
        EventContext context = contextMap.get(notification.getSource());
        if (context == null) {
            log.warn("Got a state change event for which no EventContext has been registered; source = " + notification.getSource());
        } else {
            context.publishEvent(new Event("StateChange", notification.getType(), notification.getTimeStamp(), EventSeverity.INFO, notification.getMessage()));
        }
    }
}
