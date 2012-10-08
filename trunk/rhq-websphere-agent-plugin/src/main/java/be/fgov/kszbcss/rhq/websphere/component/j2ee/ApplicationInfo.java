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

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the static information about an application, in particular the module structure and the
 * deployment descriptors.
 */
public class ApplicationInfo extends DeployedObject {
    private static final long serialVersionUID = 1917055988168366633L;
    
    private final ModuleInfo[] modules;
    
    public ApplicationInfo(byte[] deploymentDescriptor, TargetMapping[] targetMappings, ModuleInfo[] modules) {
        super(deploymentDescriptor, targetMappings);
        this.modules = modules;
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
    
    /**
     * Get the configuration of the given module.
     * 
     * @param name
     *            the module name
     * @return the module configuration, or <code>null</code> if no module with the given name
     *         exists in the application
     */
    public ModuleInfo getModule(String name) {
        for (ModuleInfo module : modules) {
            if (module.getName().equals(name)) {
                return module;
            }
        }
        return null;
    }
}
