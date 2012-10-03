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

import java.io.Serializable;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryService;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Represents a WebSphere application server. This can be a managed (federated) or unmanaged
 * (stand-alone) server.
 */
public abstract class ApplicationServer extends WebSphereServer {
    private ConfigQueryService configQueryService;
    
    public ApplicationServer(String cell, String node, String server, String processType, ProcessLocator processLocator) {
        super(cell, node, server, processType, processLocator);
    }

    @Override
    public void destroy() {
        if (configQueryService != null) {
            configQueryService.release();
            configQueryService = null;
        }
        super.destroy();
    }

    protected abstract ConfigQueryService createConfigQueryService() throws ConnectorException;
    
    public final <T extends Serializable> T queryConfig(ConfigQuery<T> query, boolean immediate) throws InterruptedException, ConnectorException {
        synchronized (this) {
            if (configQueryService == null) {
                configQueryService = createConfigQueryService();
            }
        }
        return configQueryService.query(query, immediate);
    }

    public abstract String getClusterName() throws InterruptedException, JMException, ConnectorException;
}
