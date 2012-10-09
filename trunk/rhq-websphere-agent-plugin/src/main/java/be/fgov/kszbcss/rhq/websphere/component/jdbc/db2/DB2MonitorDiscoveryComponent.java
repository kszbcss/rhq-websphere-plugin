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
package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import be.fgov.kszbcss.rhq.websphere.component.jdbc.DataSourceComponent;

public class DB2MonitorDiscoveryComponent implements ResourceDiscoveryComponent<DataSourceComponent> {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<DataSourceComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
//        if (context.getParentResourceComponent().getConnectionFactoryInfo().getProperty("clientProgramName") != null) {
        if (context.getParentResourceComponent().getConnectionFactoryInfo().getDataSourceHelper().equals("com.ibm.websphere.rsadapter.DB2UniversalDataStoreHelper")) {
            result.add(new DiscoveredResourceDetails(context.getResourceType(), "default", "DB2 Monitor", null, "DB2 Monitor", null, null));
        }
        return result;
    }
}
