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
package be.fgov.kszbcss.rhq.websphere.component;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public final class ConnectionFactoryInfo implements Serializable {
    private static final long serialVersionUID = 5754737545212010308L;
    
    private final String id;
    private final String providerName;
    private final String name;
    private final String jndiName;
    private final String dataSourceHelper;
    private final String authDataAlias;
    private final Map<String,Object> properties;
    
    ConnectionFactoryInfo(String id, String providerName, String name, String jndiName, String dataSourceHelper, String authDataAlias, Map<String,Object> properties) {
        this.id = id;
        this.providerName = providerName;
        this.name = name;
        this.jndiName = jndiName;
        this.dataSourceHelper = dataSourceHelper;
        this.authDataAlias = authDataAlias;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getName() {
        return name;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getDataSourceHelper() {
        return dataSourceHelper;
    }

    public String getAuthDataAlias() {
        return authDataAlias;
    }

    public Map<String,Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }
}
