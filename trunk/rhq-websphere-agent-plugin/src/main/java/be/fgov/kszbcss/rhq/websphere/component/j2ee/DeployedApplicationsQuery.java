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

import java.util.List;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;

import com.ibm.websphere.management.exception.ConnectorException;

public class DeployedApplicationsQuery implements ConfigQuery<String[]> {
    private static final long serialVersionUID = 3198911266754286723L;
    
    private final String node;
    private final String server;

    public DeployedApplicationsQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public String[] execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        ConfigObject serverEntry = config.node(node).path("ServerIndex").path("ServerEntry", server).resolveSingle();
        List<?> deployedApplications = (List<?>)serverEntry.getAttribute("deployedApplications");
        String[] applicationNames = new String[deployedApplications.size()];
        int i = 0;
        for (Object deployedApplication : deployedApplications) {
            String deployment = (String)deployedApplication;
            applicationNames[i++] = deployment.substring(deployment.lastIndexOf('/') + 1);
        }
        return applicationNames;
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeployedApplicationsQuery) {
            DeployedApplicationsQuery other = (DeployedApplicationsQuery)obj;
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
