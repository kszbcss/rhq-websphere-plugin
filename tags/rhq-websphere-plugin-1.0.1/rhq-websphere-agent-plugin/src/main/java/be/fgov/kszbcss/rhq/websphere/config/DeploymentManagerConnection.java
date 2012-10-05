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
package be.fgov.kszbcss.rhq.websphere.config;

import net.sf.ehcache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.DeploymentManager;

/**
 * Manages the communication with the deployment manager of a given WebSphere cell. There will be
 * one instance of this class for each cell for which there is at least one monitored WebSphere
 * instance.
 */
class DeploymentManagerConnection {
    private static final Log log = LogFactory.getLog(DeploymentManagerConnection.class);

    private final ConfigQueryServiceFactory factory;
    private final ConfigQueryServiceImpl configQueryService;
    private int refCount;
    
    DeploymentManagerConnection(ConfigQueryServiceFactory factory, CacheManager cacheManager, DeploymentManager dm, String cell) {
        this.factory = factory;
        configQueryService = new ConfigQueryServiceImpl(cacheManager, cell, dm, cell);
    }
    
    ConfigQueryServiceImpl getConfigQueryService() {
        return configQueryService;
    }
    
    synchronized void incrementRefCount() {
        refCount++;
        if (log.isDebugEnabled()) {
            log.debug("New ref count is " + refCount);
        }
    }

    synchronized void decrementRefCount() {
        refCount--;
        if (log.isDebugEnabled()) {
            log.debug("New ref count is " + refCount);
        }
        if (refCount == 0) {
            log.debug("Destroying DeploymentManagerConnection");
            configQueryService.release();
            factory.removeDeploymentManagerConnection(this);
        }
    }
}
