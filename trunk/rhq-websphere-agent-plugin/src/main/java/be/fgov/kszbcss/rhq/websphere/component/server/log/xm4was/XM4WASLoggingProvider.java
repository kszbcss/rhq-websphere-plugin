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

import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.component.server.log.EventPublisher;
import be.fgov.kszbcss.rhq.websphere.component.server.log.J2EEComponentKey;
import be.fgov.kszbcss.rhq.websphere.component.server.log.LoggingProvider;
import be.fgov.kszbcss.rhq.websphere.process.ApplicationServer;

public class XM4WASLoggingProvider implements LoggingProvider {
    private static final Log log = LogFactory.getLog(XM4WASLoggingProvider.class);
    
    private Timer timer;
    private LogEventDispatcher dispatcher;
    
    public void start(ApplicationServer server, EventContext defaultEventContext, EventPublisher eventPublisher, String state) {
        timer = new Timer();
        dispatcher = new LogEventDispatcher(server,
                server.getMBeanClient("WebSphere:*,type=XM4WAS.LoggingService").getProxy(LoggingService.class),
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
