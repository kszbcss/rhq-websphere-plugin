package be.fgov.kszbcss.rhq.websphere.component.server.log.ras;

import javax.management.NotificationFilterSupport;

import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.server.log.EventPublisher;
import be.fgov.kszbcss.rhq.websphere.component.server.log.J2EEComponentKey;
import be.fgov.kszbcss.rhq.websphere.component.server.log.LoggingProvider;
import be.fgov.kszbcss.rhq.websphere.connector.notification.NotificationListenerRegistration;

public class RasLoggingProvider implements LoggingProvider {
    private NotificationListenerRegistration registration;
    
    public void start(ApplicationServer server, EventContext defaultEventContext, EventPublisher eventPublisher, String state) {
        NotificationFilterSupport filter = new NotificationFilterSupport();
        // TODO: use constants from NotificationConstants here
        filter.enableType("websphere.ras.audit");
        filter.enableType("websphere.ras.warning");
        filter.enableType("websphere.ras.error");
        filter.enableType("websphere.ras.fatal");
        registration = server.addNotificationListener(Utils.createObjectName("WebSphere:type=RasLoggingService,*"), new RasLoggingNotificationListener(defaultEventContext, eventPublisher), filter, null, true);
    }

    public void registerEventContext(J2EEComponentKey key, EventContext context) {
        // We are not able to correlate log events with application components
    }

    public void unregisterEventContext(J2EEComponentKey key) {
    }

    public String stop() {
        registration.unregister();
        return null;
    }
}
