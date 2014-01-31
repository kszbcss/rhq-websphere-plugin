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
package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.Config;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.ApplicationDeploymentCO;
import be.fgov.kszbcss.rhq.websphere.config.types.DeploymentCO;
import be.fgov.kszbcss.rhq.websphere.config.types.PropertyCO;
import be.fgov.kszbcss.rhq.websphere.config.types.ServerEntryCO;
import be.fgov.kszbcss.rhq.websphere.config.types.ServerIndexCO;

import com.ibm.websphere.management.exception.ConnectorException;

public class DeployedApplicationsQuery implements ConfigQuery<String[]> {
    private static final long serialVersionUID = 3198911266754286723L;
    
    private static final Log log = LogFactory.getLog(DeployedApplicationsQuery.class);
    
    private final String node;
    private final String server;

    public DeployedApplicationsQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public String[] execute(Config config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        ServerEntryCO serverEntry = config.node(node).path(ServerIndexCO.class).path(ServerEntryCO.class, server).resolveSingle(false);
        List<String> deployedApplications = serverEntry.getDeployedApplications();
        List<String> result = new ArrayList<String>(deployedApplications.size());
        for (String deployment : deployedApplications) {
            String applicationName = deployment.substring(deployment.lastIndexOf('/') + 1);
            if (isLooseConfig(config.path(DeploymentCO.class, applicationName).path(ApplicationDeploymentCO.class).resolveSingle(false))) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipped application '" + applicationName + "'deployed by RAD");
                }
            } else {
                result.add(applicationName);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Determines if the given <tt>ApplicationDeployment</tt> object has a <tt>was.loose.config</tt> property.
     * This is the case for applications deployed by RAD with resources in the workspace.
     * 
     * @param applicationDeployment
     * @return <code>true</code> if the <tt>was.loose.config</tt> property is set
     * @throws JMException
     * @throws ConnectorException
     * @throws InterruptedException
     */
    private boolean isLooseConfig(ApplicationDeploymentCO applicationDeployment) throws JMException, ConnectorException, InterruptedException {
        for (PropertyCO property : applicationDeployment.getProperties()) {
            if (property.getName().equals("was.loose.config")) {
                return true;
            }
        }
        return false;
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
