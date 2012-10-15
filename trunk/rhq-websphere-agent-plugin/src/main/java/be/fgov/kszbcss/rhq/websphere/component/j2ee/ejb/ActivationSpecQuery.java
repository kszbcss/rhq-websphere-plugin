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
package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

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

public class ActivationSpecQuery implements ConfigQuery<ActivationSpecs> {
    private static final long serialVersionUID = 8754174264585470653L;
    
    private static final Log log = LogFactory.getLog(ActivationSpecQuery.class);
    
    private final String node;
    private final String server;
    
    public ActivationSpecQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public ActivationSpecs execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        Map<String,ActivationSpecInfo> map = new HashMap<String,ActivationSpecInfo>();
        for (ConfigObject ra : config.allScopes(node, server).path("J2CResourceAdapter").resolve()) {
            for (ConfigObject activationSpec : ra.getChildren("j2cActivationSpec")) {
                String jndiName = (String)activationSpec.getAttribute("jndiName");
                if (!map.containsKey(jndiName)) {
                    String destinationJndiName = (String)activationSpec.getAttribute("destinationJndiName");
                    if (destinationJndiName != null && destinationJndiName.length() == 0) {
                        destinationJndiName = null;
                    }
                    Map<String,Object> properties = new HashMap<String,Object>();
                    for (ConfigObject property : activationSpec.getChildren("resourceProperties")) {
                        properties.put((String)property.getAttribute("name"), property.getAttribute("value"));
                    }
                    map.put(jndiName, new ActivationSpecInfo(destinationJndiName, properties));
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Loaded activation specs for node '" + node + "' and server '" + server + "': " + map);
        }
        return new ActivationSpecs(map);
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActivationSpecQuery) {
            ActivationSpecQuery other = (ActivationSpecQuery)obj;
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
