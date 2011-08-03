package be.fgov.kszbcss.rhq.websphere.component.server.log.xm;

import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.server.log.EventPublisher;
import be.fgov.kszbcss.rhq.websphere.component.server.log.J2EEComponentKey;
import be.fgov.kszbcss.rhq.websphere.component.server.log.LoggingProvider;

public class ExtendedLoggingProvider implements LoggingProvider {
    private static final Log log = LogFactory.getLog(ExtendedLoggingProvider.class);
    
    private Timer timer;
    private LogEventDispatcher dispatcher;
    
    public void start(ManagedServer server, EventContext defaultEventContext, EventPublisher eventPublisher, String state) {
        timer = new Timer();
        dispatcher = new LogEventDispatcher(
                server.getMBeanClient("be.fgov.kszbcss.rhq.websphere.xm:*,type=ExtendedLoggingService").getProxy(ExtendedLoggingService.class),
                defaultEventContext, eventPublisher);
        if (state != null) {
            try {
                long sequence = Long.parseLong(state);
                if (log.isDebugEnabled()) {
                    log.debug("Setting initial log sequence from persistent state: " + sequence);
                }
                dispatcher.setSequence(sequence);
            } catch (NumberFormatException ex) {
                log.error("Unable to extract log sequence from persistent state: " + state);
            }
        }
        timer.schedule(dispatcher, 30000, 30000);
    }

    public void registerEventContext(J2EEComponentKey key, EventContext context) {
        dispatcher.registerEventContext(key, context);
    }

    public void unregisterEventContext(J2EEComponentKey key) {
        dispatcher.unregisterEventContext(key);
    }

    public String stop() {
        timer.cancel();
        return String.valueOf(dispatcher.getSequence());
    }
}
