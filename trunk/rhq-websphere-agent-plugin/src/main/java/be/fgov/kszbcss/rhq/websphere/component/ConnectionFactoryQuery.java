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

import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.ConnectionFactoryCO;
import be.fgov.kszbcss.rhq.websphere.config.types.DataSourceCO;
import be.fgov.kszbcss.rhq.websphere.config.types.J2EEResourcePropertyCO;
import be.fgov.kszbcss.rhq.websphere.config.types.J2EEResourceProviderCO;

import com.ibm.websphere.management.exception.ConnectorException;

public class ConnectionFactoryQuery implements ConfigQuery<ConnectionFactoryInfo> {
    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(ConnectionFactoryQuery.class);
    
    private final String node;
    private final String server;
    private final ConnectionFactoryType type;
    private final String jndiName;
    
    public ConnectionFactoryQuery(String node, String server, ConnectionFactoryType type, String jndiName) {
        this.node = node;
        this.server = server;
        this.type = type;
        this.jndiName = jndiName;
    }

    public ConnectionFactoryInfo execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        for (ConnectionFactoryCO cf : config.allScopes(node, server).path(type.getContainingConfigurationObjectType()).path(type.getConfigurationObjectType()).resolve(false)) {
            // Attention: the returned JNDI name may be null
            if (jndiName.equals(cf.getJndiName())) {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving properties for " + jndiName);
                }
                Map<String,Object> properties = new HashMap<String,Object>();
                for (J2EEResourcePropertyCO resourceProperty : cf.getPropertySet().getResourceProperties()) {
                    String name = resourceProperty.getName();
                    String stringValue = resourceProperty.getValue();
                    String type = resourceProperty.getType();
                    Object value;
                    // TODO: add support for other types
                    if (stringValue == null || stringValue.length() == 0 && !type.equals("java.lang.String")) {
                        value = null;
                    } else if (type == null) {
                        value = stringValue;
                    } else if (type.equals("java.lang.Integer") || type.equals("int")) {
                        value = Integer.valueOf(stringValue);
                    } else {
                        value = stringValue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("name=" + name + ", type=" + type + ", stringValue=" + stringValue
                                + ", value=" + value + ", class=" + (value == null ? "<N/A>" : value.getClass().getName()));
                    }
                    properties.put(name, value);
                }
                J2EEResourceProviderCO provider = cf.getProvider();
                return new ConnectionFactoryInfo(
                        cf.getId(),
                        provider.getName(),
                        cf.getName(),
                        jndiName,
                        cf instanceof DataSourceCO ? ((DataSourceCO)cf).getDatasourceHelperClassname() : null,
                        cf.getAuthDataAlias(),
                        properties);
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return 31*31*31*node.hashCode() + 31*31*server.hashCode() + 31*type.hashCode() + jndiName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnectionFactoryQuery) {
            ConnectionFactoryQuery other = (ConnectionFactoryQuery)obj;
            return other.node.equals(node) && other.server.equals(server) && other.type.equals(type) && other.jndiName.equals(jndiName);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + node + "," + server + "," + type + "," + jndiName + ")";
    }
}
