/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2013 Crossroads Bank for Social Security
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

final class ConfigObjectTypeRegistry {
    private static final Map<Class<? extends ConfigObject>,ConfigObjectTypeDesc> descriptorByType = new HashMap<Class<? extends ConfigObject>,ConfigObjectTypeDesc>();
    private static final Map<String,ConfigObjectTypeDesc> descriptorByName = new HashMap<String,ConfigObjectTypeDesc>();
    
    static {
        Properties mappings = new Properties();
        InputStream in = ConfigObjectTypeRegistry.class.getResourceAsStream("/META-INF/config-object-types.index");
        if (in == null) {
            throw new Error("Index file not found");
        }
        try {
            try {
                mappings.load(in);
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            throw new Error("Failed to load index file", ex);
        }
        for (Map.Entry<Object,Object> entry : mappings.entrySet()) {
            String name = (String)entry.getKey();
            Class<? extends ConfigObject> type;
            try {
                type = Class.forName((String)entry.getValue()).asSubclass(ConfigObject.class);
            } catch (ClassNotFoundException ex) {
                throw new NoClassDefFoundError(ex.getMessage());
            }
            ConfigObjectTypeDesc desc = new ConfigObjectTypeDesc(type, name);
            descriptorByType.put(type, desc);
            descriptorByName.put(name, desc);
        }
    }
    
    static ConfigObjectTypeDesc getDescriptor(Class<? extends ConfigObject> type) {
        return descriptorByType.get(type);
    }
    
    static ConfigObjectTypeDesc getDescriptor(String name) {
        return descriptorByName.get(name);
    }
}
