package be.fgov.kszbcss.rhq.websphere.connector.notification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.PIDChangeTracker;
import be.fgov.kszbcss.rhq.websphere.PIDWatcher;

import com.ibm.websphere.management.AdminClient;

/**
 * Manages JMX notification listeners registered on a given WebSphere instance. It provides the
 * following services:
 * <ul>
 * <li>If the registration of a listener fails, it will be retried later (until registration
 * succeeds).
 * <li>If the unregistration of a listener fails, it will be retried later (until unregistration
 * succeeds).
 * <li>It periodically queries the PID of the WebSphere instance. If a PID change is detected, then
 * all listeners are re-registered.
 * </ul>
 */
public class NotificationListenerManager {
    private static final Log log = LogFactory.getLog(NotificationListenerManager.class);
    
    private final AdminClient adminClient;
    private final PIDChangeTracker pidChangeTracker;
    private final List<NotificationListenerRegistration> registrations = new ArrayList<NotificationListenerRegistration>();
    private Timer timer;
    
    public NotificationListenerManager(AdminClient adminClient, PIDWatcher pidWatcher) {
        this.adminClient = adminClient;
        pidChangeTracker = pidWatcher.createTracker();
    }

    public NotificationListenerRegistration addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback, boolean extended) {
        NotificationListenerRegistration registration = new NotificationListenerRegistration(this, name, listener, filter, handback, extended);
        registration.update(adminClient);
        synchronized (registrations) {
            registrations.add(registration);
            if (timer == null) {
                log.debug("Starting notification registration timer");
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateRegistrations();
                    }
                }, 60000, 60000);
            }
        }
        return registration;
    }

    void unregister(NotificationListenerRegistration registration) {
        registration.scheduleForUnregistration();
        registration.update(adminClient);
        cleanup();
    }
    
    void updateRegistrations() {
        if (pidChangeTracker.isRestarted()) {
            if (log.isDebugEnabled()) {
                log.debug("PID change occurred; marking all listeners as unregistered");
            }
            synchronized (registrations) {
                for (NotificationListenerRegistration registration : registrations) {
                    registration.markAsUnregistered();
                }
            }
        }
        List<NotificationListenerRegistration> registrationsList;
        // Create a copy of the "registrations" collection; the JMX call may take some time and we don't
        // want to block other threads
        synchronized (registrations) {
            registrationsList = new ArrayList<NotificationListenerRegistration>(registrations);
        }
        for (NotificationListenerRegistration registration : registrationsList) {
            registration.update(adminClient);
        }
        cleanup();
    }
    
    private void cleanup() {
        synchronized (registrations) {
            for (Iterator<NotificationListenerRegistration> it = registrations.iterator(); it.hasNext(); ) {
                if (it.next().isRemoved()) {
                    it.remove();
                }
            }
            if (timer != null && registrations.isEmpty()) {
                log.debug("Stopping notification registration timer");
                timer.cancel();
                timer = null;
            }
        }
    }
}
