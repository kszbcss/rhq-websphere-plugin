/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012,2014 Crossroads Bank for Social Security
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.config.Config;

public class ModuleInfoFactory {
    private static final Logger log = LoggerFactory.getLogger(ModuleInfoFactory.class);
    
    private static final Map<String,ModuleInfoFactory> factories;
    
    static {
        factories = new HashMap<String,ModuleInfoFactory>();
        factories.put("WebModuleDeployment", new ModuleInfoFactory(ModuleType.WEB, "WEB-INF", "web"));
        factories.put("EJBModuleDeployment", new ModuleInfoFactory(ModuleType.EJB, "META-INF", "ejb-jar"));
    }
    
    private final ModuleType type;
    private final String infPath;
    private final String deploymentDescriptorName;
    
    public ModuleInfoFactory(ModuleType type, String infPath, String deploymentDescriptorName) {
        this.type = type;
        this.infPath = infPath;
        this.deploymentDescriptorName = deploymentDescriptorName;
    }

    public static ModuleInfoFactory getInstance(String configDataType) {
        return factories.get(configDataType);
    }
    
    public String locateDeploymentDescriptor(Config config, String moduleURI) throws JMException, ConnectorException, InterruptedException {
        if (config.getWebSphereVersion().startsWith("6.")) {
            log.debug("Server implements J2EE 1.4; returning URI of static deployment descriptor");
        } else {
            String[] resources = config.listResourceNames(moduleURI + "/" + infPath, 1, 1);
            if (log.isDebugEnabled()) {
                log.debug("Deployment descriptor list: " + Arrays.asList(resources));
            }
            String merged = search(resources, "/" + deploymentDescriptorName + "_merged.xml");
            if (merged != null) {
                log.debug("Merged deployment descriptor found");
                return merged;
            }
        }
        return moduleURI + "/" + infPath + "/" + deploymentDescriptorName + ".xml";
    }
    
    private static String search(String[] list, String suffix) {
        for (String item : list) {
            if (item.endsWith(suffix)) {
                return item;
            }
        }
        return null;
    }
    
    public ModuleInfo create(String name, byte[] deploymentDescriptor, TargetMapping[] targetMappings) {
        return new ModuleInfo(type, name, deploymentDescriptor, targetMappings);
    }
}
