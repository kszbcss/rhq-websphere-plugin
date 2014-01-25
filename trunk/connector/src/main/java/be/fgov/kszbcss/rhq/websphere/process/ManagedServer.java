/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2014 Crossroads Bank for Social Security
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
package be.fgov.kszbcss.rhq.websphere.process;

import javax.management.JMException;

import org.rhq.core.domain.configuration.Configuration;

import be.fgov.kszbcss.rhq.websphere.config.ConfigData;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryService;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryServiceFactory;
import be.fgov.kszbcss.rhq.websphere.process.locator.ConfigurationBasedProcessLocator;

import com.ibm.websphere.management.exception.ConnectorException;

public final class ManagedServer extends ApplicationServer {
    private final ConfigQuery<String> clusterNameQuery;
    private final ConfigData<String> clusterName;
    private NodeAgent nodeAgent;
    
    /**
     * Constructor.
     * 
     * @param cell
     *            the cell name; must not be <code>null</code>
     * @param node
     *            the node name; must not be <code>null</code>
     * @param server
     *            the server name; must not be <code>null</code>
     * @param config
     */
    public ManagedServer(String cell, String node, String server, Configuration config) {
        super(cell, node, server, "ManagedProcess", new ConfigurationBasedProcessLocator(config));
        clusterName = registerConfigQuery(clusterNameQuery = new ClusterNameQuery(node, server));
    }

    @Override
    public void destroy() {
        unregisterConfigQuery(clusterNameQuery);
        super.destroy();
    }

    public synchronized NodeAgent getNodeAgent() {
        if (nodeAgent == null) {
            nodeAgent = new NodeAgent(getCell(), getNode(), new ParentProcessLocator(this));
        }
        return nodeAgent;
    }
    
    @Override
    protected ConfigQueryService createConfigQueryService() {
        return ConfigQueryServiceFactory.getInstance().getConfigQueryService(this);
    }

    public String getClusterName() throws InterruptedException, JMException, ConnectorException, ConfigQueryException {
        return clusterName.get();
    }
}
