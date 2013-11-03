/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2013 Crossroads Bank for Social Security
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

import be.fgov.kszbcss.rhq.websphere.config.types.ConnectionFactoryCO;
import be.fgov.kszbcss.rhq.websphere.config.types.DataSourceCO;
import be.fgov.kszbcss.rhq.websphere.config.types.J2CConnectionFactoryCO;
import be.fgov.kszbcss.rhq.websphere.config.types.J2CResourceAdapterCO;
import be.fgov.kszbcss.rhq.websphere.config.types.J2EEResourceProviderCO;
import be.fgov.kszbcss.rhq.websphere.config.types.JDBCProviderCO;

import com.ibm.websphere.pmi.PmiConstants;

public enum ConnectionFactoryType {
    JDBC(DataSourceCO.class, JDBCProviderCO.class, "DataSource", PmiConstants.CONNPOOL_MODULE),
    J2C(J2CConnectionFactoryCO.class, J2CResourceAdapterCO.class, "J2CConnectionFactory", PmiConstants.J2C_MODULE);
    
    private final Class<? extends ConnectionFactoryCO> configurationObjectType;
    private final Class<? extends J2EEResourceProviderCO> containingConfigurationObjectType;
    private final String mbeanType;
    private final String pmiModule;
    
    private ConnectionFactoryType(Class<? extends ConnectionFactoryCO> configurationObjectType, Class<? extends J2EEResourceProviderCO> containingConfigurationObjectType, String mbeanType, String pmiModule) {
        this.configurationObjectType = configurationObjectType;
        this.containingConfigurationObjectType = containingConfigurationObjectType;
        this.mbeanType = mbeanType;
        this.pmiModule = pmiModule;
    }

    public Class<? extends ConnectionFactoryCO> getConfigurationObjectType() {
        return configurationObjectType;
    }

    public Class<? extends J2EEResourceProviderCO> getContainingConfigurationObjectType() {
        return containingConfigurationObjectType;
    }

    public String getMBeanType() {
        return mbeanType;
    }

    public String getPmiModule() {
        return pmiModule;
    }
}
