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
package be.fgov.kszbcss.rhq.websphere.mbean;

import javax.management.JMException;
import javax.management.ObjectName;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on an object name pattern. This implementation assumes that the pattern is
 * known when the locator is constructed.
 */
public class StaticMBeanObjectNamePatternLocator extends MBeanObjectNamePatternLocator {
    private final ObjectName pattern;
    
    public StaticMBeanObjectNamePatternLocator(ObjectName pattern, boolean recursive) {
        super(recursive);
        this.pattern = pattern;
    }

    public StaticMBeanObjectNamePatternLocator(ObjectName pattern) {
        this(pattern, false);
    }
    
    @Override
    protected ObjectName getPattern(WebSphereServer server) throws JMException, ConnectorException {
        return pattern;
    }

    @Override
    public String toString() {
        return pattern.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StaticMBeanObjectNamePatternLocator
                && pattern.equals(((StaticMBeanObjectNamePatternLocator)obj).pattern);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }
}
