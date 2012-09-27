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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.rhq.websphere.component.server.log.EventPublisher;

import com.ibm.ejs.ras.RasMessageImpl2;
import com.ibm.websphere.ras.RasMessage;

public class RasLoggingNotificationListener implements NotificationListener {
    public static final String EVENT_TYPE = "RasMessage";
    
    private static final Map<String,EventSeverity> severityMap = new HashMap<String,EventSeverity>();
    private static final Field localizedMessageField;
    
    static {
        severityMap.put(RasMessage.AUDIT, EventSeverity.INFO);
        severityMap.put(RasMessage.WARNING, EventSeverity.WARN);
        severityMap.put(RasMessage.ERROR, EventSeverity.ERROR);
        severityMap.put(RasMessage.FATAL, EventSeverity.FATAL);
        try {
            localizedMessageField = RasMessageImpl2.class.getDeclaredField("ivLocalizedMessage");
        } catch (NoSuchFieldException ex) {
            throw new NoSuchFieldError(ex.getMessage());
        }
        localizedMessageField.setAccessible(true);
    }
    
    private final EventContext eventContext;
    private final EventPublisher eventPublisher;
    
    public RasLoggingNotificationListener(EventContext eventContext, EventPublisher eventPublisher) {
        this.eventContext = eventContext;
        this.eventPublisher = eventPublisher;
    }
    
    public void handleNotification(Notification notification, Object handback) {
        RasMessage rasMessage = (RasMessage)notification.getUserData();
        // We extract the localized message using reflection because
        // getLocalizedMessage will always compare the locale. If there is a
        // locale mismatch and the necessary resource bundle is not found,
        // no message will be returned.
        String localizedMessage;
        try {
            localizedMessage = (String)localizedMessageField.get(rasMessage);
        } catch (IllegalAccessException ex) {
            throw new IllegalAccessError(ex.getMessage());
        }
        eventPublisher.publishEvent(eventContext, rasMessage.getMessageOriginator(), rasMessage.getTimeStamp(),
                severityMap.get(rasMessage.getMessageSeverity()), localizedMessage);
    }
}
