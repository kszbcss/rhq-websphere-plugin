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
package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public abstract class SIBLocalizationPointDiscoveryComponent implements ResourceDiscoveryComponent<SIBMessagingEngineComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<SIBMessagingEngineComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        SIBMessagingEngineComponent me = context.getParentResourceComponent();
        SIBMessagingEngineInfo meInfo = me.getInfo();
        if (meInfo == null) {
            throw new InvalidPluginConfigurationException("Messaging engine " + me.getName() + " not found");
        }
        for (String destination : meInfo.getDestinationNames(getType())) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), destination, destination, null, "SIBus Destination", null, null));
        }
        return result;
    }

    protected abstract SIBLocalizationPointType getType();
}
