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
package be.fgov.kszbcss.rhq.websphere.component.pme;

import java.util.HashMap;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.WorkManagerInfoCO;
import be.fgov.kszbcss.rhq.websphere.config.types.WorkManagerProviderCO;

import com.ibm.websphere.management.exception.ConnectorException;

public class WorkManagerMapQuery implements ConfigQuery<HashMap<String,String>> {
    private static final long serialVersionUID = 949198396112347229L;
    
    private static final Log log = LogFactory.getLog(WorkManagerMapQuery.class);
    
    private final String node;
    private final String server;
    
    public WorkManagerMapQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public HashMap<String,String> execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        HashMap<String,String> map = new HashMap<String,String>();
        for (WorkManagerInfoCO wm : config.allScopes(node, server).path(WorkManagerProviderCO.class).path(WorkManagerInfoCO.class).resolve()) {
            String jndiName = wm.getJndiName();
            if (!map.containsKey(jndiName)) {
                map.put(jndiName, wm.getName());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Loaded work managers for node '" + node + "' and server '" + server + "': " + map);
        }
        return map;
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkManagerMapQuery) {
            WorkManagerMapQuery other = (WorkManagerMapQuery)obj;
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
