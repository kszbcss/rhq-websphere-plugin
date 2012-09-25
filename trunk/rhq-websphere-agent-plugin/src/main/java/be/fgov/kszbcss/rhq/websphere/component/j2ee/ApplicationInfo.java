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
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

/**
 * Contains the static information about an application, in particular the module structure and the
 * deployment descriptors.
 */
public class ApplicationInfo implements Serializable {
    private static final long serialVersionUID = -8427058440507167719L;
    
    private final DeploymentDescriptor deploymentDescriptor;
    private final ModuleInfo[] modules;
    
    public ApplicationInfo(byte[] deploymentDescriptor, ModuleInfo[] modules) {
        this.deploymentDescriptor = deploymentDescriptor == null ? null : new DeploymentDescriptor(deploymentDescriptor);
        this.modules = modules;
    }
    
    public Document getDeploymentDescriptor() {
        return deploymentDescriptor == null ? null : deploymentDescriptor.getDOM();
    }
    
    public List<ModuleInfo> getModules(ModuleType type) {
        List<ModuleInfo> result = new ArrayList<ModuleInfo>();
        for (ModuleInfo module : modules) {
            if (module.getType() == type) {
                result.add(module);
            }
        }
        return result;
    }
    
    public ModuleInfo getModule(String name) {
        for (ModuleInfo module : modules) {
            if (module.getName().equals(name)) {
                return module;
            }
        }
        return null;
    }
}
