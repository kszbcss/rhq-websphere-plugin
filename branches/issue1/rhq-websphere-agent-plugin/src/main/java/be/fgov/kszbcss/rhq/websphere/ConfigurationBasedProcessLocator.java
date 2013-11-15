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
package be.fgov.kszbcss.rhq.websphere;

import java.util.Properties;

import org.rhq.core.domain.configuration.Configuration;

import com.ibm.websphere.management.AdminClient;

/**
 * {@link ProcessLocator} implementation that builds the admin client
 * configuration from a plugin configuration.
 */
public class ConfigurationBasedProcessLocator extends ProcessLocator {
    private final Configuration config;

    public ConfigurationBasedProcessLocator(Configuration config) {
        this.config = config;
    }

    public void getAdminClientProperties(Properties properties) {
        properties.put(AdminClient.CONNECTOR_TYPE, config.getSimpleValue("protocol", "RMI"));
        properties.setProperty(AdminClient.CONNECTOR_HOST, config.getSimpleValue("host", null));
        properties.setProperty(AdminClient.CONNECTOR_PORT, config.getSimpleValue("port", null));
        
        String principal = config.getSimpleValue("principal", null); 
        if (principal != null && principal.length() > 0) { 
            properties.setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED, "true"); 
            properties.setProperty(AdminClient.USERNAME, principal); 
            properties.setProperty(AdminClient.PASSWORD, config.getSimpleValue("credentials", null)); 
        } else { 
            properties.setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED, "false");
        }
    }
}
