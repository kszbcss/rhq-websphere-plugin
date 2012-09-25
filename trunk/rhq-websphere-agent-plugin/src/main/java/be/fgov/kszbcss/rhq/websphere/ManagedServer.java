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

import javax.management.JMException;

import org.rhq.core.domain.configuration.Configuration;

import be.fgov.kszbcss.rhq.websphere.component.server.ClusterNameQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryService;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryServiceFactory;

import com.ibm.websphere.management.exception.ConnectorException;

public class ManagedServer extends ApplicationServer {
    private NodeAgent nodeAgent;
    
    public ManagedServer(String cell, String node, String server, Configuration config) {
        super(cell, node, server, "ManagedProcess", new ConfigurationBasedProcessLocator(config));
    }

    public synchronized NodeAgent getNodeAgent() throws ConnectorException {
        if (nodeAgent == null) {
            nodeAgent = new NodeAgent(getCell(), getNode(), new ParentProcessLocator(this));
        }
        return nodeAgent;
    }
    
    @Override
    protected ConfigQueryService createConfigQueryService() throws ConnectorException {
        return ConfigQueryServiceFactory.getInstance().getConfigQueryService(getNodeAgent().getDeploymentManager());
    }

    public String getClusterName() throws InterruptedException, JMException, ConnectorException {
        return queryConfig(new ClusterNameQuery(getNode(), getServer()), false);
    }
}
