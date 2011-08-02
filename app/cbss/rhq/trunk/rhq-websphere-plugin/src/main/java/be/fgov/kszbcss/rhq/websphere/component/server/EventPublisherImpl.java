package be.fgov.kszbcss.rhq.websphere.component.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.event.Event;
import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.server.log.EventPublisher;

public class EventPublisherImpl implements EventPublisher {
    public static final EventPublisherImpl INSTANCE = new EventPublisherImpl();
    
    private static final Log log = LogFactory.getLog(EventPublisherImpl.class);
    
    private final Object lastSanitizeWarningLock = new Object();
    private long lastSanitizeWarning;
    
    private EventPublisherImpl() {}
    
    public void publishEvent(EventContext context, String sourceLocation, long timestamp, EventSeverity severity, String detail) {
        StringBuilder buffer = null;
        for (int i=0; i<detail.length(); i++) {
            char c = detail.charAt(i);
            if (c < 32 && c != '\r' && c != '\n' && c != '\t') {
                if (buffer == null) {
                    buffer = new StringBuilder(detail.length());
                    buffer.append(detail, 0, i);
                }
                buffer.append(" ");
            } else if (buffer != null) {
                buffer.append(c);
            }
        }
        if (buffer != null) {
            detail = buffer.toString();
            if (log.isWarnEnabled()) {
                // We only log a warning every 5 minutes
                synchronized (lastSanitizeWarningLock) {
                    long time = System.currentTimeMillis();
                    if (time - lastSanitizeWarning > 300000) {
                        lastSanitizeWarning = time;
                        log.warn("Got a RasMessage with invalid characters from " + sourceLocation
                                + " (severity: " + severity + "): " + detail);
                    }
                }
            }
        }
        Utils.publishEvent(context, new Event("LogEvent", sourceLocation, timestamp, severity, detail));
    }
}
