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

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

public class MBeanClientFactory {
    private static final Log log = LogFactory.getLog(MBeanClientFactory.class);
    
    private final WebSphereServer server;
    private final Map<MBeanLocator,MBeanClient> cache = new HashMap<MBeanLocator,MBeanClient>();
    
    public MBeanClientFactory(WebSphereServer server) {
        this.server = server;
    }
    
    /**
     * Get a client for the MBean identified by the given locator. This method may either return a
     * cached instance or create a new one. Note that since the MBean is located lazily (during the
     * first invocation), this method will never fail, even if the connection to the server is
     * unavailable or if no matching MBean is registered.
     * 
     * @param locator
     *            the locator identifying the MBean
     * @return the client instance
     */
    public MBeanClient getMBeanClient(MBeanLocator locator) {
        synchronized (cache) {
            MBeanClient client = cache.get(locator);
            if (client == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating new MBeanClient for locator: " + locator);
                }
                client = new MBeanClient(server, locator);
                cache.put(locator, client);
            }
            return client;
        }
    }

    public MBeanClient getMBeanClient(ObjectName objectNamePattern) {
        return getMBeanClient(new StaticMBeanObjectNamePatternLocator(objectNamePattern));
    }
    
    public MBeanClient getMBeanClient(String objectNamePattern) {
        try {
            return getMBeanClient(new ObjectName(objectNamePattern));
        } catch (MalformedObjectNameException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
