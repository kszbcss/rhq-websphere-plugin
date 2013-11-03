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

final class ConfigObjectAttributeDesc {
    private final String name;
    private final Class<?> type;
    private final boolean collection;
    
    ConfigObjectAttributeDesc(String name, Class<?> type, boolean collection) {
        this.name = name;
        this.type = type;
        this.collection = collection;
    }

    String getName() {
        return name;
    }

    boolean isReference() {
        return ConfigObject.class.isAssignableFrom(type);
    }

    boolean isCollection() {
        return collection;
    }
    
    ConfigObjectTypeDesc getDescriptor() {
        return ConfigObjectTypeRegistry.getDescriptor(type.asSubclass(ConfigObject.class));
    }
}
