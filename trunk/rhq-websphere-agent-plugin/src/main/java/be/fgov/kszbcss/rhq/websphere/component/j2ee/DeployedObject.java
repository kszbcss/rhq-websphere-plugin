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

import java.io.Serializable;

import javax.management.JMException;

import org.w3c.dom.Document;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;

public abstract class DeployedObject implements Serializable {
    private static final long serialVersionUID = -5263858554100368909L;
    
    private final DeploymentDescriptor deploymentDescriptor;
    private final TargetMapping[] targetMappings;

    public DeployedObject(byte[] deploymentDescriptor, TargetMapping[] targetMappings) {
        this.deploymentDescriptor = deploymentDescriptor == null ? null : new DeploymentDescriptor(deploymentDescriptor);
        this.targetMappings = targetMappings;
    }

    public final Document getDeploymentDescriptor() {
        return deploymentDescriptor == null ? null : deploymentDescriptor.getDOM();
    }
    
    public final TargetMapping getTargetMapping(ApplicationServer server) throws ConnectorException, InterruptedException, JMException {
        for (TargetMapping mapping : targetMappings) {
            if (mapping.getTarget().matches(server)) {
                return mapping;
            }
        }
        return null;
    }
}
