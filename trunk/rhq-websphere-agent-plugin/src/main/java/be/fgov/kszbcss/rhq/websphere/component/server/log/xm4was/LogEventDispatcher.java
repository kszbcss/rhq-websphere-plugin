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
package be.fgov.kszbcss.rhq.websphere.component.server.log.xm4was;

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
import be.fgov.kszbcss.rhq.websphere.process.WebSphereServer;

import com.ibm.websphere.logging.WsLevel;

class LogEventDispatcher extends TimerTask {
    public static final String EVENT_TYPE = "LogEvent";
    
    private static final Log log = LogFactory.getLog(LogEventDispatcher.class);
    
    private final WebSphereServer server;
    private final LoggingService service;
    private final EventContext defaultEventContext;
    private final EventPublisher eventPublisher;
    private final Map<J2EEComponentKey,EventContext> eventContexts = Collections.synchronizedMap(new HashMap<J2EEComponentKey,EventContext>());
    private volatile long sequence = -1;
    private boolean threadNameUpdated;
    
    LogEventDispatcher(WebSphereServer server, LoggingService service, EventContext defaultEventContext, EventPublisher eventPublisher) {
        this.server = server;
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
            if (!threadNameUpdated) {
                Thread.currentThread().setName("log-poller-" + server.getCell() + "-" + server.getNode() + "-" + server.getServer());
                threadNameUpdated = true;
            }
            if (sequence == -1) {
                sequence = service.getNextSequence();
                if (log.isDebugEnabled()) {
                    log.debug("Got initial log sequence from server: " + sequence);
                }
            } else {
                // TODO: detect sequence gaps and generate an event so that the user knows that events have been dropped
                String[] formattedMessages = service.getMessages(sequence, 4000); // rhq_event.detail is varchar(4000)
                long firstSequence = -1;
                long lastSequence = -1;
                for (String formattedMessage : formattedMessages) {
                    LogMessage message = new LogMessage(formattedMessage);
                    lastSequence = message.getSequence();
                    if (firstSequence == -1) {
                        firstSequence = lastSequence;
                    }
                    String applicationName = message.getApplicationName();
                    String moduleName = message.getModuleName();
                    String componentName = message.getComponentName();
                    EventContext eventContext;
                    if (applicationName == null || moduleName == null) {
                        eventContext = defaultEventContext;
                    } else {
                        J2EEComponentKey key = new J2EEComponentKey(applicationName, moduleName, componentName);
                        // Note: the value may be null (in which case we don't publish the event)
                        if (eventContexts.containsKey(key)) {
                            eventContext = eventContexts.get(key);
                        } else {
                            eventContext = defaultEventContext;
                        }
                    }
                    if (eventContext == null) {
                        eventPublisher.eventSuppressed();
                    } else {
                        eventPublisher.publishEvent(eventContext, message.getLoggerName(), message.getTimestamp(),
                                convertLevel(message.getLevel()), message.getMessage());
                    }
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
