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

import be.fgov.kszbcss.rhq.websphere.config.ConfigData;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;

final class ConfigDataImpl<T extends Serializable> implements ConfigData<T> {
    private final ConfigQueryCache cache;
    private final ConfigQueryCacheEntry<T> entry;
    
    ConfigDataImpl(ConfigQueryCache cache, ConfigQueryCacheEntry<T> entry) {
        this.cache = cache;
        this.entry = entry;
    }

    @Override
    public T get() throws InterruptedException, ConfigQueryException {
        return cache.get(entry);
    }
}
