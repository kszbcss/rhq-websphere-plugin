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

import java.io.Serializable;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryService;
import be.fgov.kszbcss.rhq.websphere.process.locator.ProcessLocator;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Represents a WebSphere application server. This can be a managed (federated) or unmanaged
 * (stand-alone) server.
 */
public abstract class ApplicationServer extends WebSphereServer {
    private final String cell;
    private final String node;
    private final String server;
    private ConfigQueryService configQueryService;
    
    /**
     * Constructor.
     * 
     * @param cell
     *            the cell name; must not be <code>null</code>
     * @param node
     *            the node name; must not be <code>null</code>
     * @param server
     *            the server name; must not be <code>null</code>
     * @param processType
     * @param processLocator
     */
    public ApplicationServer(String cell, String node, String server, String processType, ProcessLocator processLocator) {
        super(cell, node, server, processType, processLocator);
        if (cell == null || node == null || server == null) {
            throw new IllegalArgumentException();
        }
        this.cell = cell;
        this.node = node;
        this.server = server;
    }

    @Override
    public final String getCell() {
        return cell;
    }

    @Override
    public final String getNode() {
        return node;
    }

    @Override
    public final String getServer() {
        return server;
    }

    @Override
    public void destroy() {
        if (configQueryService != null) {
            configQueryService.release();
            configQueryService = null;
        }
        super.destroy();
    }

    protected abstract ConfigQueryService createConfigQueryService();
    
    public final <T extends Serializable> T queryConfig(ConfigQuery<T> query) throws InterruptedException, ConnectorException, ConfigQueryException {
        synchronized (this) {
            if (configQueryService == null) {
                configQueryService = createConfigQueryService();
            }
        }
        return configQueryService.query(query);
    }

    public abstract String getClusterName() throws InterruptedException, JMException, ConnectorException, ConfigQueryException;
}
