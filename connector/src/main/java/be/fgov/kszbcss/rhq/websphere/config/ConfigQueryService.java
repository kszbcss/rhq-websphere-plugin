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

import java.io.Serializable;

// TODO: Javadoc is no longer accurate
/**
 * Supports sending queries for configuration data to a deployment manager. There is a single
 * instance of this class for each WebSphere cell with at least one server being monitored by the
 * plugin. Query results are stored in a cache.
 * <p>
 * <b>Note:</b> The implementation is designed such that a single cache instance can be used for all
 * cells (i.e. the cache key contains the cell name). This makes sure that cache entries eventually
 * disappear when all servers for a given cell are removed from the inventory.
 */
public interface ConfigQueryService {
    <T extends Serializable> ConfigData<T> registerConfigQuery(ConfigQuery<T> query);
    
    void unregisterConfigQuery(ConfigQuery<?> query);
    
    void release();
}
