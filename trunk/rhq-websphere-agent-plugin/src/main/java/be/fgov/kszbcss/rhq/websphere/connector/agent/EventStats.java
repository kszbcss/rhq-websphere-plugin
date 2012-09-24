package be.fgov.kszbcss.rhq.websphere.connector.agent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects metrics about the received log events. These metrics are exposed by
 * {@link ConnectorSubsystemComponent}.
 */
public final class EventStats {
    private static final AtomicLong logEventsPublished = new AtomicLong();
    private static final AtomicLong logEventsSuppressed = new AtomicLong();
    
    public static void incrementEventsPublished() {
        logEventsPublished.incrementAndGet();
    }
    
    public static void incrementEventsSuppressed() {
        logEventsSuppressed.incrementAndGet();
    }
    
    public static long getLogEventsPublished() {
        return logEventsPublished.get();
    }
    
    public static long getLogEventsSuppressed() {
        return logEventsSuppressed.get();
    }
}
