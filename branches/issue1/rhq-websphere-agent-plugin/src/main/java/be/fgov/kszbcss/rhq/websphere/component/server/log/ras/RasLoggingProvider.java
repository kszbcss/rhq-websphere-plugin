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
package be.fgov.kszbcss.rhq.websphere.component.server.log.ras;

import javax.management.NotificationFilterSupport;

import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.server.log.EventPublisher;
import be.fgov.kszbcss.rhq.websphere.component.server.log.J2EEComponentKey;
import be.fgov.kszbcss.rhq.websphere.component.server.log.LoggingProvider;
import be.fgov.kszbcss.rhq.websphere.connector.notification.NotificationListenerRegistration;

public class RasLoggingProvider implements LoggingProvider {
    private NotificationListenerRegistration registration;
    
    public void start(ApplicationServer server, EventContext defaultEventContext, EventPublisher eventPublisher, String state) {
        NotificationFilterSupport filter = new NotificationFilterSupport();
        // TODO: use constants from NotificationConstants here
        filter.enableType("websphere.ras.audit");
        filter.enableType("websphere.ras.warning");
        filter.enableType("websphere.ras.error");
        filter.enableType("websphere.ras.fatal");
        registration = server.addNotificationListener(Utils.createObjectName("WebSphere:type=RasLoggingService,*"), new RasLoggingNotificationListener(defaultEventContext, eventPublisher), filter, null, true);
    }

    public void registerEventContext(J2EEComponentKey key, EventContext context) {
        // We are not able to correlate log events with application components
    }

    public void unregisterEventContext(J2EEComponentKey key) {
    }

    public String stop() {
        registration.unregister();
        return null;
    }
}
