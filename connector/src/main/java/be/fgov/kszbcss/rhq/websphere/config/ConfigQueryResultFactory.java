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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.cache.CacheRefreshException;
import be.fgov.kszbcss.rhq.websphere.config.cache.DelayedRefreshCacheEntryFactory;

import com.ibm.websphere.management.repository.ConfigEpoch;

class ConfigQueryResultFactory implements DelayedRefreshCacheEntryFactory<ConfigQuery<?>,ConfigQueryResult> {
    private static final Log log = LogFactory.getLog(ConfigQueryResultFactory.class);
    
    private final ConfigQueryServiceImpl configQueryServiceImpl;
    
    public ConfigQueryResultFactory(ConfigQueryServiceImpl configQueryServiceImpl) {
        this.configQueryServiceImpl = configQueryServiceImpl;
    }

    public ConfigQueryResult createEntry(ConfigQuery<?> key) throws CacheRefreshException {
        ConfigEpoch epoch = configQueryServiceImpl.getEpoch();
        if (epoch == null) {
            throw new CacheRefreshException("Deployment manager is unavailable");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + key);
            }
            ConfigQueryResult result = new ConfigQueryResult();
            result.epoch = epoch;
            try {
                result.object = key.execute(configQueryServiceImpl.getCellConfiguration());
            } catch (Exception ex) {
                // TODO: review this
                throw new CacheRefreshException(ex);
            }
            return result;
        }
    }

    public boolean isStale(ConfigQuery<?> key, ConfigQueryResult value) {
        ConfigEpoch epoch = configQueryServiceImpl.getEpoch();
        if (epoch == null) {
            if (log.isDebugEnabled()) {
                log.debug("Deployment manager is unavailable; returning potentially stale object for query: " + key);
            }
            return false;
        } else {
            ConfigQueryResult result = (ConfigQueryResult)value;
            if (result.epoch.equals(epoch)) {
                if (log.isDebugEnabled()) {
                    log.debug("Not updating result for the following query: " + key);
                }
                return false;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Reexecuting query: " + key);
                }
                return true;
            }
        }
    }
}
