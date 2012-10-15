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
package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;

import com.ibm.websphere.management.exception.ConnectorException;

public class SIBDestinationMapQuery implements ConfigQuery<SIBDestinationMap> {
    private static final long serialVersionUID = 1542229435338345886L;
    
    private static final Log log = LogFactory.getLog(SIBDestinationMapQuery.class);
    
    private final String node;
    private final String server;
    
    public SIBDestinationMapQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public SIBDestinationMap execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        Map<String,SIBDestination> map = new HashMap<String,SIBDestination>();
        for (ConfigObject ra : config.allScopes(node, server).path("J2CResourceAdapter").resolve()) {
            for (ConfigObject adminObject : ra.getChildren("j2cAdminObjects")) {
                String jndiName = (String)adminObject.getAttribute("jndiName");
                if (!map.containsKey(jndiName)) {
                    String busName = null;
                    String destinationName = null;
                    for (ConfigObject property : adminObject.getChildren("properties")) {
                        String propName = (String)property.getAttribute("name");
                        if (propName.equals("BusName")) {
                            busName = (String)property.getAttribute("value");
                        } else if (propName.equals("QueueName")) {
                            destinationName = (String)property.getAttribute("value");
                        }
                    }
                    map.put(jndiName, new SIBDestination(busName, destinationName));
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Loaded SIB destinations for node '" + node + "' and server '" + server + "': " + map);
        }
        return new SIBDestinationMap(map);
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SIBDestinationMapQuery) {
            SIBDestinationMapQuery other = (SIBDestinationMapQuery)obj;
            return other.node.equals(node) && other.server.equals(server);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + node + "," + server + ")";
    }
}
