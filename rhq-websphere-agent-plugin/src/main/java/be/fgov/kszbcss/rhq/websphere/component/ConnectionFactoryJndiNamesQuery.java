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
package be.fgov.kszbcss.rhq.websphere.component;

import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.Config;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.ConnectionFactoryCO;

import com.ibm.websphere.management.exception.ConnectorException;

public class ConnectionFactoryJndiNamesQuery implements ConfigQuery<String[]> {
    private static final long serialVersionUID = 1L;
    
    private final String node;
    private final String server;
    private final ConnectionFactoryType type;
    
    public ConnectionFactoryJndiNamesQuery(String node, String server, ConnectionFactoryType type) {
        this.node = node;
        this.server = server;
        this.type = type;
    }

    public String[] execute(Config config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        Set<String> result = new HashSet<String>();
        for (ConnectionFactoryCO cf : config.allScopes(node, server).path(type.getContainingConfigurationObjectType()).path(type.getConfigurationObjectType()).resolve(false)) {
            String jndiName = cf.getJndiName();
            // If no JNDI name is defined, then it's probably a J2CConnectionFactory corresponding to a JDBC data source
            if (jndiName != null) {
                result.add(jndiName);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public int hashCode() {
        return 31*31*node.hashCode() + 31*server.hashCode() + type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnectionFactoryJndiNamesQuery) {
            ConnectionFactoryJndiNamesQuery other = (ConnectionFactoryJndiNamesQuery)obj;
            return other.node.equals(node) && other.server.equals(server) && other.type.equals(type);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + node + "," + server + "," + type + ")";
    }
}
