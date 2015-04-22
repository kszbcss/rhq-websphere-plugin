/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012,2014 Crossroads Bank for Social Security
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

import java.io.File;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.fgov.kszbcss.rhq.websphere.process.DeploymentManager;

/**
 * Manages the communication with the deployment manager of a given WebSphere cell. There will be
 * one instance of this class for each cell for which there is at least one monitored WebSphere
 * instance.
 */
class DeploymentManagerConnection {
    private static final Logger log = LoggerFactory.getLogger(DeploymentManagerConnection.class);

    private final ConfigQueryServiceFactory factory;
    private final ConfigQueryServiceImpl configQueryService;
    private int refCount;
    
    DeploymentManagerConnection(ConfigQueryServiceFactory factory, DeploymentManager dm, String cell, File persistentFile) {
        this.factory = factory;
        configQueryService = new ConfigQueryServiceImpl(cell, persistentFile, dm, cell);
    }
    
    ConfigQueryServiceImpl getConfigQueryService() {
        return configQueryService;
    }
    
	private synchronized void incrementRefCount() {
        refCount++;
        if (log.isDebugEnabled()) {
            log.debug("New ref count is " + refCount);
        }
    }

	private synchronized void decrementRefCount() {
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

	public ConfigQueryService createConfigQueryServiceHandle() {
		incrementRefCount();
		return new ConfigQueryServiceHandle();
	}

	private class ConfigQueryServiceHandle implements ConfigQueryService {

		@Override
		public <T extends Serializable> ConfigData<T> registerConfigQuery(ConfigQuery<T> query) {
			return configQueryService.registerConfigQuery(query);
		}

		@Override
		public void unregisterConfigQuery(ConfigQuery<?> query) {
			configQueryService.unregisterConfigQuery(query);
		}

		@Override
		public void release() {
			if (log.isDebugEnabled()) {
				log.debug("Releasing one instance of ConfigQueryServiceHandle for cell "
						+ configQueryService.getCell());
			}
			decrementRefCount();
		}
	}

}
