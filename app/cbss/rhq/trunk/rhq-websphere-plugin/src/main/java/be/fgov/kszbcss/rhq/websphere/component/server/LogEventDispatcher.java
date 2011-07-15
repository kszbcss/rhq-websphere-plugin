package be.fgov.kszbcss.rhq.websphere.component.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Level;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.event.Event;
import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.xm.logging.ExtendedLogMessage;

import com.ibm.websphere.logging.WsLevel;

class LogEventDispatcher extends TimerTask {
    public static final String EVENT_TYPE = "LogEvent";
    
    private static final Log log = LogFactory.getLog(LogEventDispatcher.class);
    
    private static final Object lastSanitizeWarningLock = new Object();
    private static long lastSanitizeWarning;
    
    private final ExtendedLoggingService service;
    private final EventContext defaultEventContext;
    private final Map<J2EEComponentKey,EventContext> eventContexts = Collections.synchronizedMap(new HashMap<J2EEComponentKey,EventContext>());
    private long sequence;
    
    LogEventDispatcher(ExtendedLoggingService service, EventContext defaultEventContext) {
        this.service = service;
        this.defaultEventContext = defaultEventContext;
    }
    
    void registerEventContext(J2EEComponentKey key, EventContext context) {
        eventContexts.put(key, context);
    }
    
    void unregisterEventContext(J2EEComponentKey key) {
        eventContexts.remove(key);
    }

    @Override
    public void run() {
        try {
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
                Utils.publishEvent(eventContext, new Event(EVENT_TYPE, message.getLoggerName(), message.getTimestamp(),
                        convertLevel(message.getLevel()), sanitizeMessage(message, message.getMessage())));
            }
            if (firstSequence != -1) {
                if (log.isDebugEnabled()) {
                    log.debug("Fetched log events with sequences " + firstSequence + "..." + lastSequence);
                }
                sequence = lastSequence+1;
            }
        } catch (Throwable ex) {
            log.error("Failed to poll server for log events", ex);
        }
    }
    
    // TODO: copy & paste from RasLoggingNotificationListener
    private static String sanitizeMessage(ExtendedLogMessage message, String text) {
        StringBuilder buffer = null;
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            if (c < 32 && c != '\r' && c != '\n' && c != '\t') {
                if (buffer == null) {
                    buffer = new StringBuilder(text.length());
                    buffer.append(text, 0, i);
                }
                buffer.append(" ");
            } else if (buffer != null) {
                buffer.append(c);
            }
        }
        if (buffer == null) {
            return text;
        } else {
            String sanitizedText = buffer.toString();
            if (log.isWarnEnabled()) {
                // We only log a warning every 5 minutes
                synchronized (lastSanitizeWarningLock) {
                    long time = System.currentTimeMillis();
                    if (time - lastSanitizeWarning > 300000) {
                        lastSanitizeWarning = time;
                        log.warn("Got a log message with invalid characters from " + message.getLoggerName()
                                + " (severity: " + message.getLevel() + "): " + sanitizedText);
                    }
                }
            }
            return sanitizedText;
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
