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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public abstract class ModuleDiscoveryComponent implements ResourceDiscoveryComponent<ApplicationComponent> {
    private static final Logger log = LoggerFactory.getLogger(ModuleDiscoveryComponent.class);
    
    public final Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<ApplicationComponent> context) throws InvalidPluginConfigurationException, Exception {
        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        ApplicationInfo applicationInfo = context.getParentResourceComponent().getApplicationInfo();
        List<ModuleInfo> modules = applicationInfo.getModules(getModuleType());
        if (log.isDebugEnabled()) {
            log.debug("Found " + modules.size() + " module(s) of type " + getModuleType() + " in application " + context.getParentResourceComponent().getApplicationName());
        }
        for (ModuleInfo module : modules) {
            String name = module.getName();
            if (module.getTargetMapping(context.getParentResourceComponent().getServer()) == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Module " + name + " is not mapped to server");
                }
            } else {
                result.add(new DiscoveredResourceDetails(context.getResourceType(), name, name, null, getDescription(name), null, null));
            }
        }
        return result;
    }
    
    protected abstract ModuleType getModuleType();
    protected abstract String getDescription(String moduleName);
}
