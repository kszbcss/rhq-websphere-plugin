/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012 Crossroads Bank for Social Security
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package be.fgov.kszbcss.rhq.websphere.component.server.log;

import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
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
     * @param state
     *            the persisted state of the logging provider as returned by {@link #stop()}
     */
    void start(ApplicationServer server, EventContext defaultEventContext, EventPublisher eventPublisher, String state);
    
    /**
     * Register an {@link EventContext} for a given J2EE application component. Implementations that
     * are able to correlate log events with application components should use this information to
     * dispatch the event.
     * 
     * @param key
     *            the key identifying the J2EE application component
     * @param context
     *            the event context or <code>null</code> if no events should be generated for this
     *            component
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
     * 
     * @return A string value that will be passed to
     *         {@link #start(ApplicationServer, EventContext, EventPublisher, String)} the next time the
     *         logging provider is started. This may be used to persist the sequence number of the
     *         last log event. The implementation may return <code>null</code>.
     */
    String stop();
}
