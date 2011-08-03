package be.fgov.kszbcss.rhq.websphere.component.server.log.xm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Level;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.component.server.log.EventPublisher;
import be.fgov.kszbcss.rhq.websphere.component.server.log.J2EEComponentKey;
import be.fgov.kszbcss.rhq.websphere.xm.logging.ExtendedLogMessage;

import com.ibm.websphere.logging.WsLevel;

class LogEventDispatcher extends TimerTask {
    public static final String EVENT_TYPE = "LogEvent";
    
    private static final Log log = LogFactory.getLog(LogEventDispatcher.class);
    
    private final ExtendedLoggingService service;
    private final EventContext defaultEventContext;
    private final EventPublisher eventPublisher;
    private final Map<J2EEComponentKey,EventContext> eventContexts = Collections.synchronizedMap(new HashMap<J2EEComponentKey,EventContext>());
    private volatile long sequence = -1;
    
    LogEventDispatcher(ExtendedLoggingService service, EventContext defaultEventContext, EventPublisher eventPublisher) {
        this.service = service;
        this.defaultEventContext = defaultEventContext;
        this.eventPublisher = eventPublisher;
    }
    
    void registerEventContext(J2EEComponentKey key, EventContext context) {
        eventContexts.put(key, context);
    }
    
    void unregisterEventContext(J2EEComponentKey key) {
        eventContexts.remove(key);
    }

    long getSequence() {
        return sequence;
    }

    void setSequence(long sequence) {
        this.sequence = sequence;
    }

    @Override
    public void run() {
        try {
            if (sequence == -1) {
                sequence = service.getNextSequence();
                if (log.isDebugEnabled()) {
                    log.debug("Got initial log sequence from server: " + sequence);
                }
            } else {
                // TODO: detect sequence gaps and generate an event so that the user knows that events have been dropped
                ExtendedLogMessage[] messages = service.getMessages(sequence);
                long firstSequence = -1;
                long lastSequence = -1;
                for (ExtendedLogMessage message : messages) {
                    lastSequence = message.getSequence();
                    if (firstSequence == -1) {
                        firstSequence = lastSequence;
                    }
                    String applicationName = message.getApplicationName();
                    String moduleName = message.getModuleName();
                    String componentName = message.getComponentName();
                    EventContext eventContext;
                    if (applicationName == null || moduleName == null || componentName == null) {
                        eventContext = defaultEventContext;
                    } else {
                        eventContext = eventContexts.get(new J2EEComponentKey(applicationName, moduleName, componentName));
                        if (eventContext == null) {
                            eventContext = defaultEventContext;
                        }
                    }
                    eventPublisher.publishEvent(eventContext, message.getLoggerName(), message.getTimestamp(),
                            convertLevel(message.getLevel()), message.getMessage());
                }
                if (firstSequence != -1) {
                    if (log.isDebugEnabled()) {
                        log.debug("Fetched log events with sequences " + firstSequence + "..." + lastSequence);
                    }
                    sequence = lastSequence+1;
                }
            }
        } catch (Throwable ex) {
            log.error("Failed to poll server for log events", ex);
        }
    }
    
    private EventSeverity convertLevel(int level) {
        if (level < Level.INFO.intValue()) {
            return EventSeverity.DEBUG;
        } else if (level < Level.WARNING.intValue()) {
            return EventSeverity.INFO;
        } else if (level < Level.SEVERE.intValue()) {
            return EventSeverity.WARN;
        } else if (level < WsLevel.FATAL.intValue()) {
            return EventSeverity.ERROR;
        } else {
            return EventSeverity.FATAL;
        }
    }
}
