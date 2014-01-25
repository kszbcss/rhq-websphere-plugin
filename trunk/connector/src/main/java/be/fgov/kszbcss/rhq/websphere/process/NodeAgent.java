/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2014 Crossroads Bank for Social Security
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
package be.fgov.kszbcss.rhq.websphere.process;

import be.fgov.kszbcss.rhq.websphere.process.locator.ProcessLocator;

public final class NodeAgent extends WebSphereServer {
    private final String cell;
    private final String node;
    private DeploymentManager deploymentManager;
    
    /**
     * Constructor.
     * 
     * @param cell
     *            the cell name; must not be <code>null</code>
     * @param node
     *            the node name; must not be <code>null</code>
     * @param processLocator
     */
    public NodeAgent(String cell, String node, ProcessLocator processLocator) {
        super(cell, node, "nodeagent", "NodeAgent", processLocator);
        if (cell == null || node == null) {
            throw new IllegalArgumentException();
        }
        this.cell = cell;
        this.node = node;
    }

    @Override
    public String getCell() {
        return cell;
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public String getServer() {
        return "nodeagent";
    }

    public synchronized DeploymentManager getDeploymentManager() {
        if (deploymentManager == null) {
            deploymentManager = new DeploymentManager(getCell(), new ParentProcessLocator(this));
        }
        return deploymentManager;
    }
}
