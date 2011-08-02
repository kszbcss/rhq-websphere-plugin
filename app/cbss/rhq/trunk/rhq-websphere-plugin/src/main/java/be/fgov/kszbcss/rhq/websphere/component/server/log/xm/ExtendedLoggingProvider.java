package be.fgov.kszbcss.rhq.websphere.component.server.log.xm;

import java.util.Timer;

import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.server.log.EventPublisher;
import be.fgov.kszbcss.rhq.websphere.component.server.log.J2EEComponentKey;
import be.fgov.kszbcss.rhq.websphere.component.server.log.LoggingProvider;

public class ExtendedLoggingProvider implements LoggingProvider {
    private Timer timer;
    private LogEventDispatcher dispatcher;
    
    public void start(ManagedServer server, EventContext defaultEventContext, EventPublisher eventPublisher) {
        timer = new Timer();
        dispatcher = new LogEventDispatcher(
                server.getMBeanClient("be.fgov.kszbcss.rhq.websphere.xm:*,type=ExtendedLoggingService").getProxy(ExtendedLoggingService.class),
                defaultEventContext, eventPublisher);
        timer.schedule(dispatcher, 30000, 30000);
    }

    public void registerEventContext(J2EEComponentKey key, EventContext context) {
        dispatcher.registerEventContext(key, context);
    }

    public void unregisterEventContext(J2EEComponentKey key) {
        dispatcher.unregisterEventContext(key);
    }

    public void stop() {
        timer.cancel();
    }
}
