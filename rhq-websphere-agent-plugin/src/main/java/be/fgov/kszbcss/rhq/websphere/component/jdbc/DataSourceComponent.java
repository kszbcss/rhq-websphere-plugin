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
package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;

import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryComponent;
import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryType;
import be.fgov.kszbcss.rhq.websphere.support.configuration.ConfigurationFacetSupport;

public class DataSourceComponent extends ConnectionFactoryComponent implements ConfigurationFacet {
    private static final Logger log = LoggerFactory.getLogger(DataSourceComponent.class);
    
    private ConfigurationFacetSupport configurationFacetSupport;
    
    @Override
    protected ConnectionFactoryType getType() {
        return ConnectionFactoryType.JDBC;
    }

    @Override
    protected void doStart() throws InvalidPluginConfigurationException {
        super.doStart();
        configurationFacetSupport = new ConfigurationFacetSupport(this, mbean);
    }
    
    public Configuration loadResourceConfiguration() throws Exception {
        // Data source are initialized lazily upon first lookup. If a data source has not been initialized yet,
        // some configuration attributes are unavailable. This state can be detected by checking the dataSourceName
        // attribute.
        if (mbean.getAttribute("dataSourceName") == null) {
            if (log.isDebugEnabled()) {
                log.debug("Data source " + jndiName + " is not initialized; configuration will not be loaded");
            }
            // There seems to be no way to let the plugin container know that the configuration
            // is not available. But the plugin container code does a null check (and throws an exception).
            return null;
        } else {
            return configurationFacetSupport.loadResourceConfiguration();
        }
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
        configurationFacetSupport.updateResourceConfiguration(report);
    }
}
