package be.fgov.kszbcss.rhq.websphere.component.server.log;

import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;

/**
 * Retrieves and dispatches log events from a remote server. There is one instance of
 * {@link LoggingProvider} for each {@link WebSphereServerComponent}. The implementation is selected
 * based on the configuration of that component.
 */
public interface LoggingProvider {
    /**
     * Start the logging provider for the given server.
     * 
     * @param server
     *            the server to collect log events from
     * @param defaultEventContext
     *            the default {@link EventContext} to be used for events that are not correlated
     *            with any application component or for which no {@link EventContext} has been
     *            registered with {@link #registerEventContext(J2EEComponentKey, EventContext)}
     * @param eventPublisher
     *            the event publisher to use
     */
    void start(ManagedServer server, EventContext defaultEventContext, EventPublisher eventPublisher);
    
    /**
     * Register an {@link EventContext} for a given J2EE application component. Implementations that
     * are able to correlate log events with application components should use this information to
     * dispatch the event.
     * 
     * @param key
     *            the key identifying the J2EE application component
     * @param context
     *            the event context
     */
    void registerEventContext(J2EEComponentKey key, EventContext context);
    
    /**
     * Inform the provider that the resource component for the given J2EE application component has
     * been stopped and that the {@link EventContext} previously registered with
     * {@link #registerEventContext(J2EEComponentKey, EventContext)} will no longer be valid.
     * 
     * @param key
     *            the key identifying the J2EE application component
     */
    void unregisterEventContext(J2EEComponentKey key);
    
    /**
     * Stop the logging provider.
     */
    void stop();
}
