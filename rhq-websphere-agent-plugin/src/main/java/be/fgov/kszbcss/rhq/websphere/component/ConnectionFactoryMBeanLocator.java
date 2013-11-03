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
package be.fgov.kszbcss.rhq.websphere.component;

import java.util.Map;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.mbean.DynamicMBeanObjectNamePatternLocator;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * MBean locator implementation for connection factories. This locator uses the MBean identifier
 * derived from the configuration data to locate the corresponding MBean. The reason is that
 * WebSphere doesn't enforce the uniqueness of the (scope, provider name, data source name) triplet
 */
public final class ConnectionFactoryMBeanLocator extends DynamicMBeanObjectNamePatternLocator {
    private final ConnectionFactoryType type;
    private final String jndiName;

    public ConnectionFactoryMBeanLocator(ConnectionFactoryType type, String jndiName) {
        super("WebSphere", false);
        this.type = type;
        this.jndiName = jndiName;
    }

    @Override
    protected void applyKeyProperties(WebSphereServer server, Map<String,String> props) throws JMException, ConnectorException, InterruptedException {
        ConnectionFactoryInfo cf;
        try {
            cf = ((ApplicationServer)server).queryConfig(new ConnectionFactoryQuery(server.getNode(), server.getServer(), type)).getByJndiName(jndiName);
        } catch (ConfigQueryException ex) {
            // TODO
            throw new RuntimeException(ex);
        }
        if (cf == null) {
            throw new JMException("A " + type.getConfigurationObjectType() + " with JNDI name " + jndiName + " doesn't exist in the configuration");
        }
        props.put("type", type.getMBeanType());
        props.put("mbeanIdentifier", cf.getId().replace('|', '/'));
    }
    
    // TODO: implement equals and hashCode

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + type + "," + jndiName + ")";
    }
}
