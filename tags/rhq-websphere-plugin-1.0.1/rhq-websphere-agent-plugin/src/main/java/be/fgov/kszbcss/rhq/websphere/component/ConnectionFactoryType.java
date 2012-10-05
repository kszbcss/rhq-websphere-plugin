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

import com.ibm.websphere.pmi.PmiConstants;

public enum ConnectionFactoryType {
    JDBC("DataSource", "JDBCProvider", PmiConstants.CONNPOOL_MODULE),
    J2C("J2CConnectionFactory", "J2CResourceAdapter", PmiConstants.J2C_MODULE);
    
    private final String configurationObjectType;
    private final String containingConfigurationObjectType;
    private final String pmiModule;
    
    private ConnectionFactoryType(String configurationObjectType, String containingConfigurationObjectType, String pmiModule) {
        this.configurationObjectType = configurationObjectType;
        this.containingConfigurationObjectType = containingConfigurationObjectType;
        this.pmiModule = pmiModule;
    }

    public String getConfigurationObjectType() {
        return configurationObjectType;
    }

    public String getContainingConfigurationObjectType() {
        return containingConfigurationObjectType;
    }

    public String getPmiModule() {
        return pmiModule;
    }
}
