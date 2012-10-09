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

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfigQueryServiceHandle implements ConfigQueryService {
    private static final Log log = LogFactory.getLog(ConfigQueryServiceHandle.class);
    
    private DeploymentManagerConnection dmc;
    
    ConfigQueryServiceHandle(DeploymentManagerConnection dmc) {
        this.dmc = dmc;
    }
    
    public <T extends Serializable> T query(ConfigQuery<T> query) throws InterruptedException {
        return dmc.getConfigQueryService().query(query);
    }
    
    public void release() {
        if (log.isDebugEnabled()) {
            log.debug("Releasing one instance of ConfigQueryServiceHandle for cell " + dmc.getConfigQueryService().getCell());
        }
        dmc.decrementRefCount();
        dmc = null;
    }
}
