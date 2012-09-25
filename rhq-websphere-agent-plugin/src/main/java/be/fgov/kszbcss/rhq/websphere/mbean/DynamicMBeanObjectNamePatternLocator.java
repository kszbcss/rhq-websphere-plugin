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

import java.util.Hashtable;
import java.util.Map;

import javax.management.JMException;
import javax.management.ObjectName;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on an object name pattern constructed dynamically. This implementation
 * should be used when the pattern is not known at construction time and can only be determined
 * later.
 */
public abstract class DynamicMBeanObjectNamePatternLocator extends MBeanObjectNamePatternLocator {
    private final String domain;
    
    public DynamicMBeanObjectNamePatternLocator(String domain, boolean recursive) {
        super(recursive);
        this.domain = domain;
    }

    @Override
    protected final ObjectName getPattern(WebSphereServer server) throws JMException, ConnectorException, InterruptedException {
        Hashtable<String,String> props = new Hashtable<String,String>();
        applyKeyProperties(server, props);
        return new ObjectName(new ObjectName(domain, props).toString() + ",*");
    }
    
    protected abstract void applyKeyProperties(WebSphereServer server, Map<String,String> props) throws JMException, ConnectorException, InterruptedException;
}
