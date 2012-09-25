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

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.proxy.Discovery;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.ws.management.discovery.ServerInfo;

public class ParentProcessLocator extends ProcessLocator {
    private WebSphereServer server;

    public ParentProcessLocator(WebSphereServer server) {
        this.server = server;
    }

    public void getAdminClientProperties(Properties properties) throws JMException, ConnectorException {
        server.getProcessLocator().getAdminClientProperties(properties);
        String connectorType = properties.getProperty(AdminClient.CONNECTOR_TYPE);
        ServerInfo parent = server.getMBeanClient("WebSphere:type=Discovery,*").getProxy(Discovery.class).getParent();
        if (parent == null) {
            throw new JMException("Parent process not available");
        }
        for (Properties connector : parent.getConnectors()) {
            if (connector.getProperty(AdminClient.CONNECTOR_TYPE).equals(connectorType)) {
                properties.setProperty(AdminClient.CONNECTOR_HOST, connector.getProperty(AdminClient.CONNECTOR_HOST));
                properties.setProperty(AdminClient.CONNECTOR_PORT, connector.getProperty(AdminClient.CONNECTOR_PORT));
            }
        }
    }
}
