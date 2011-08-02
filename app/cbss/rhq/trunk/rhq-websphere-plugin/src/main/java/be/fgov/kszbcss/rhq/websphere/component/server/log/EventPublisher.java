package be.fgov.kszbcss.rhq.websphere.component.server.log;

import org.rhq.core.domain.event.Event;
import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.pluginapi.event.EventContext;

/**
 * Encapsulates the event publication logic. {@link LoggingProvider} implementations should use this
 * instead of {@link EventContext#publishEvent(Event)}. It implements things such as sanitizing the
 * message.
 */
public interface EventPublisher {
    void publishEvent(EventContext context, String sourceLocation, long timestamp, EventSeverity severity, String detail);
}
