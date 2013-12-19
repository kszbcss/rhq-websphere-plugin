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

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.WorkManagerInfoCO;
import be.fgov.kszbcss.rhq.websphere.config.types.WorkManagerProviderCO;

import com.ibm.websphere.management.exception.ConnectorException;

public class WorkManagerQuery implements ConfigQuery<WorkManagerInfoCO> {
    private static final long serialVersionUID = 1L;
    
    private final String node;
    private final String server;
    private final String jndiName;
    
    public WorkManagerQuery(String node, String server, String jndiName) {
        this.node = node;
        this.server = server;
        this.jndiName = jndiName;
    }

    public WorkManagerInfoCO execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        for (WorkManagerInfoCO wm : config.allScopes(node, server).path(WorkManagerProviderCO.class).path(WorkManagerInfoCO.class).resolve(false)) {
            if (jndiName.equals(wm.getJndiName())) {
                wm.detach();
                return wm;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return 31*31*node.hashCode() + 31*server.hashCode() + jndiName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkManagerQuery) {
            WorkManagerQuery other = (WorkManagerQuery)obj;
            return other.node.equals(node) && other.server.equals(server) && other.jndiName.equals(jndiName);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + node + "," + server + "," + jndiName + ")";
    }
}
