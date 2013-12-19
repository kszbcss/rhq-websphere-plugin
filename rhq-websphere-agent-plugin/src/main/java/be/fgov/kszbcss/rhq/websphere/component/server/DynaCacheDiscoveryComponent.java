/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2013 Crossroads Bank for Social Security
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
package be.fgov.kszbcss.rhq.websphere.component.server;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public class DynaCacheDiscoveryComponent implements ResourceDiscoveryComponent<WebSphereServerComponent> {
    private static final Log log = LogFactory.getLog(DynaCacheDiscoveryComponent.class); 
    
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        for (String instanceName : context.getParentResourceComponent().getObjectCacheInstanceNames()) {
            if (instanceName.equals("baseCache") || instanceName.startsWith("ws/")) {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring DynaCache " + instanceName);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Discovered DynaCache " + instanceName);
                }
                result.add(new DiscoveredResourceDetails(context.getResourceType(), instanceName, instanceName, null, instanceName, null, null));
            }
        }
        return result;
    }
}
