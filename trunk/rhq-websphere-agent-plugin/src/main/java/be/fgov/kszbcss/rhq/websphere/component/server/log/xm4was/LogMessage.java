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

public class LogMessage {
    private final long sequence;
    private final int level;
    private final long timestamp;
    private final String loggerName;
    private final String applicationName;
    private final String moduleName;
    private final String componentName;
    private final String message;
    
    public LogMessage(String formattedMessage) {
        int idx = formattedMessage.indexOf(']');
        String[] fields = formattedMessage.substring(1, idx).split(":", -1);
        sequence = Long.parseLong(fields[0]);
        level = Integer.parseInt(fields[1]);
        timestamp = Long.parseLong(fields[2]);
        loggerName = fields[3];
        applicationName = emptyToNull(fields[4]);
        moduleName = emptyToNull(fields[5]);
        componentName = emptyToNull(fields[6]);
        message = formattedMessage.substring(idx+1);
    }
    
    private static String emptyToNull(String s) {
        return s.length() == 0 ? null : s;
    }
    
    public long getSequence() {
        return sequence;
    }

    public int getLevel() {
        return level;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getMessage() {
        return message;
    }
}
