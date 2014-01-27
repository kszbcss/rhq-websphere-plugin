/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2014 Crossroads Bank for Social Security
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
package be.fgov.kszbcss.rhq.websphere.config.cache;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;

import com.ibm.websphere.management.repository.ConfigEpoch;

final class ConfigQueryCacheEntry<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The config query.
     */
    final ConfigQuery<T> query;
    
    /**
     * The repository epoch at which the query was executed. If this is <code>null</code>, then the
     * query has not yet been executed.
     */
    ConfigEpoch epoch;

    /**
     * The config query result. This may be <code>null</code> if one of the following conditions is
     * true:
     * <ul>
     * <li>{@link #epoch} is <code>null</code>, i.e. the query has not been executed yet.
     * <li>{@link #nonTransientException} is not <code>null</code>, i.e. the query execution failed
     * with a non transient error.
     * <li>The query returned <code>null</code>.
     * </ul>
     */
    T result;
    
    /**
     * The non transient {@link ConfigQueryException} that was triggered the last time the query was
     * executed.
     */
    ConfigQueryException nonTransientException;
    
    /**
     * The number of times the config query has been registered.
     */
    transient int refCount;
    
    /**
     * Threads waiting for a refresh of the query result.
     */
    transient final Set<Thread> waitingThreads = new HashSet<Thread>();
    
    /**
     * Indicates that the entry is currently being refreshed. This field is guarded by
     * <code>this</code> and {@link ConfigQueryCache#cache}.
     */
    transient boolean refreshing;
    
    /**
     * The timestamp of the last transient error that prevented the refresh of this entry, or 0 if
     * the last refresh attempt succeeded.
     */
    transient long lastTransientError;

    ConfigQueryCacheEntry(ConfigQuery<T> query) {
        this.query = query;
    }
}
