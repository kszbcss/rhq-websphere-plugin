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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;

/**
 * Support class to collect the deployment configuration for a WAR module or an EJB.
 */
public class DeploymentConfigurationFacetSupport {
    private static final Log log = LogFactory.getLog(DeploymentConfigurationFacetSupport.class);
    
    private static final Set<String> destinationTypes = new HashSet<String>(Arrays.asList("javax.jms.Queue", "javax.jms.Topic", "javax.jms.Destination"));
    
    private final ApplicationComponent application;
    private final String moduleName;
    private final String beanName;
    
    public DeploymentConfigurationFacetSupport(ApplicationComponent application, String moduleName, String beanName) {
        this.application = application;
        this.moduleName = moduleName;
        this.beanName = beanName;
    }

    public void loadResourceConfiguration(Configuration configuration) throws Exception {
        ApplicationConfiguration appConfig = application.getConfiguration();
        
        // Normally, messaging destination references are declared using a specific type of entry in the
        // deployment descriptor (message-destination-ref). However, lot of people don't
        // understand J2EE and (incorrectly) use a resource or resource environment reference.
        // We need to collect all of them.
        Map<String,String> messagingDestinationRefs = new HashMap<String,String>();
        List<Map<String,String>> data = appConfig.getData("MapMessageDestinationRefToEJB", moduleName, beanName);
        if (data != null) {
            for (Map<String,String> entry : data) {
                messagingDestinationRefs.put(entry.get("messageDestinationRefName"), entry.get("JNDI"));
            }
        }
        data = appConfig.getData("MapResRefToEJB", moduleName, beanName);
        if (data != null) {
            for (Map<String,String> entry : data) {
                if (destinationTypes.contains(entry.get("resRef.type"))) {
                    messagingDestinationRefs.put(entry.get("referenceBinding"), entry.get("JNDI"));
                }
            }
        }
        data = appConfig.getData("MapResEnvRefToRes", moduleName, beanName);
        if (data != null) {
            for (Map<String,String> entry : data) {
                if (destinationTypes.contains(entry.get("resEnvRef.type"))) {
                    messagingDestinationRefs.put(entry.get("referenceBinding"), entry.get("JNDI"));
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Messaging destination bindings: " + messagingDestinationRefs);
        }
        if (!messagingDestinationRefs.isEmpty()) {
            SIBDestinationMap sibDestinationMap = application.getParent().getSIBDestinationMap();
            PropertyList list = new PropertyList("messagingDestinationRefs");
            for (Map.Entry<String,String> entry : messagingDestinationRefs.entrySet()) {
                PropertyMap map = new PropertyMap("messagingDestinationRef");
                // WebSphere doesn't seem to worry about extra spaces around the JNDI names.
                // Trim the strings so that we get the same result as WebSphere.
                String refName = entry.getKey().trim();
                String jndiName = entry.getValue().trim();
                map.put(new PropertySimple("name", refName));
                map.put(new PropertySimple("bindingName", jndiName));
                SIBDestination dest = sibDestinationMap.getSIBDestination(jndiName);
                if (dest != null) {
                    map.put(new PropertySimple("busName", dest.getBusName()));
                    map.put(new PropertySimple("destinationName", dest.getDestinationName()));
                }
                list.add(map);
                if (log.isDebugEnabled()) {
                    log.debug("Discovered binding: " + map);
                }
            }
            configuration.put(list);
        }
    }
}
