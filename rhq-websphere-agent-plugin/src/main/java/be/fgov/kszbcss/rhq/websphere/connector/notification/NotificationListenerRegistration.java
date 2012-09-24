package be.fgov.kszbcss.rhq.websphere.connector.notification;

import javax.management.ListenerNotFoundException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;

public class NotificationListenerRegistration {
    private static final int UNREGISTERED = 0;
    private static final int REGISTERED = 1;

    private static final Log log = LogFactory.getLog(NotificationListenerRegistration.class);
    
    private final NotificationListenerManager manager;
    private final ObjectName name;
    private final NotificationListener listener;
    private final NotificationFilter filter;
    private final Object handback;
    private final boolean extended;
    private int desiredState;
    private int actualState;
    
    NotificationListenerRegistration(NotificationListenerManager manager, ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback, boolean extended) {
        this.manager = manager;
        this.name = name;
        this.listener = listener;
        this.filter = filter;
        this.handback = handback;
        this.extended = extended;
        desiredState = REGISTERED;
        actualState = UNREGISTERED;
    }
    
    synchronized void markAsUnregistered() {
        actualState = UNREGISTERED;
    }
    
    synchronized void scheduleForUnregistration() {
        desiredState = UNREGISTERED;
    }

    synchronized boolean isRemoved() {
        return desiredState == UNREGISTERED && actualState == UNREGISTERED;
    }
    
    synchronized void update(AdminClient adminClient) {
        if (actualState != desiredState) {
            if (desiredState == REGISTERED) {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to register notification listener on " + name);
                }
                try {
                    if (extended) {
                        adminClient.addNotificationListenerExtended(name, listener, filter, handback);
                    } else {
                        adminClient.addNotificationListener(name, listener, filter, handback);
                    }
                    actualState = REGISTERED;
                } catch (Exception ex) {
                    log.debug("Listener registration failed; will retry later", ex);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to unregister notification listener on " + name);
                }
                try {
                    try {
                        if (extended) {
                            adminClient.removeNotificationListenerExtended(name, listener);
                        } else {
                            adminClient.removeNotificationListener(name, listener);
                        }
                    } catch (ListenerNotFoundException ex) {
                        log.debug("Listener appears to be no longer registered");
                    }
                    actualState = UNREGISTERED;
                } catch (Exception ex) {
                    log.debug("Listener unregistration failed; will retry later", ex);
                }
            }
        }
    }
    
    public void unregister() {
        manager.unregister(this);
    }
}
