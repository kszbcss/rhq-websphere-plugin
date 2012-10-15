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
package be.fgov.kszbcss.rhq.websphere.component.pme;

import java.util.HashMap;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;

import com.ibm.websphere.management.exception.ConnectorException;

public class TimerManagerMapQuery implements ConfigQuery<HashMap<String,String>> {
    private static final long serialVersionUID = -2899860553017447788L;

    private static final Log log = LogFactory.getLog(TimerManagerMapQuery.class);
    
    private final String node;
    private final String server;
    
    public TimerManagerMapQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public HashMap<String,String> execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        HashMap<String,String> map = new HashMap<String,String>();
        for (ConfigObject wm : config.allScopes(node, server).path("TimerManagerProvider").path("TimerManagerInfo").resolve()) {
            String jndiName = (String)wm.getAttribute("jndiName");
            if (!map.containsKey(jndiName)) {
                map.put(jndiName, (String)wm.getAttribute("name"));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Loaded timer managers for node '" + node + "' and server '" + server + "': " + map);
        }
        return map;
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimerManagerMapQuery) {
            TimerManagerMapQuery other = (TimerManagerMapQuery)obj;
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
