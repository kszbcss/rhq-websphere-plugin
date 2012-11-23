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
package be.fgov.kszbcss.rhq.websphere;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.connector.AdminClientProvider;

class ProcessIdentityValidator implements AdminClientProvider {
    private static final Log log = LogFactory.getLog(ProcessIdentityValidator.class);
    
    private final AdminClientProvider parent;
    private AdminClient adminClient;
    private boolean initialized;
    private String cell;
    private String node;
    private String server;
    private String processType;
    
    ProcessIdentityValidator(AdminClientProvider parent, String cell,
            String node, String server, String processType) {
        this.parent = parent;
        this.cell = cell;
        this.node = node;
        this.server = server;
        this.processType = processType;
    }

    private synchronized void init() throws ConnectorException {
        if (adminClient == null) {
            adminClient = parent.createAdminClient();
        }
        if (!initialized) {
            ObjectName serverMBean = adminClient.getServerMBean();
            String actualCell = serverMBean.getKeyProperty("cell");
            String actualNode = serverMBean.getKeyProperty("node");
            String actualServer = serverMBean.getKeyProperty("process");
            String actualProcessType = serverMBean.getKeyProperty("processType");
            compare("cell name", cell, actualCell);
            compare("node name", node, actualNode);
            compare("server name", server, actualServer);
            compare("process type", processType, actualProcessType);
            cell = actualCell;
            node = actualNode;
            server = actualServer;
            processType = actualProcessType;
            initialized = true;
        }
    }
    
    private static void compare(String property, String expected, String actual) throws ConnectorException {
        if (log.isDebugEnabled()) {
            log.debug("Checking " + property + ": expected=" + (expected == null ? "<unspecified>" : expected) + ", actual=" + actual);
        }
        if (expected != null && !actual.equals(expected)) {
            throw new ConnectorException("The WebSphere process doesn't have the expected " + property + ": expected=" + expected + ", actual=" + actual);
        }
    }

    synchronized String getCell() throws ConnectorException {
        if (cell == null) {
            init();
        }
        return cell;
    }

    synchronized String getNode() throws ConnectorException {
        if (node == null) {
            init();
        }
        return node;
    }

    synchronized String getServer() throws ConnectorException {
        if (server == null) {
            init();
        }
        return server;
    }

    synchronized String getProcessType() throws ConnectorException {
        if (processType == null) {
            init();
        }
        return processType;
    }

    public synchronized AdminClient createAdminClient() throws ConnectorException {
        init();
        return adminClient;
    }
}
