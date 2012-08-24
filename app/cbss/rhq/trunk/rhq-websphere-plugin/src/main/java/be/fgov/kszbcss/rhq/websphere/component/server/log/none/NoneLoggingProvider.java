package be.fgov.kszbcss.rhq.websphere.component.server.log.none;

import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.component.server.log.EventPublisher;
import be.fgov.kszbcss.rhq.websphere.component.server.log.J2EEComponentKey;
import be.fgov.kszbcss.rhq.websphere.component.server.log.LoggingProvider;

/**
 * Dummy {@link LoggingProvider} implementation that does nothing.
 */
public class NoneLoggingProvider implements LoggingProvider {
    public void start(ApplicationServer server, EventContext defaultEventContext, EventPublisher eventPublisher, String state) {
    }

    public void registerEventContext(J2EEComponentKey key, EventContext context) {
    }

    public void unregisterEventContext(J2EEComponentKey key) {
    }

    public String stop() {
        return null;
    }
}
