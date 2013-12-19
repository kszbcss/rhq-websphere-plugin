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

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.ApplicationDeploymentCO;
import be.fgov.kszbcss.rhq.websphere.config.types.ClusteredTargetCO;
import be.fgov.kszbcss.rhq.websphere.config.types.DeployedObjectCO;
import be.fgov.kszbcss.rhq.websphere.config.types.DeploymentCO;
import be.fgov.kszbcss.rhq.websphere.config.types.DeploymentTargetCO;
import be.fgov.kszbcss.rhq.websphere.config.types.DeploymentTargetMappingCO;
import be.fgov.kszbcss.rhq.websphere.config.types.ModuleDeploymentCO;
import be.fgov.kszbcss.rhq.websphere.config.types.ServerTargetCO;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.management.exception.DocumentNotFoundException;

public class ApplicationInfoQuery implements ConfigQuery<ApplicationInfo> {
    private static final long serialVersionUID = 2L;

    private static final Log log = LogFactory.getLog(ApplicationInfoQuery.class);
    
    private final String applicationName;

    public ApplicationInfoQuery(String applicationName) {
        this.applicationName = applicationName;
    }

    public ApplicationInfo execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        ApplicationDeploymentCO applicationDeployment = config.path(DeploymentCO.class, applicationName).path(ApplicationDeploymentCO.class).resolveAtMostOne(false);
        if (applicationDeployment == null) {
            return null;
        } else {
            String dataId = applicationDeployment.getId();
            String baseURI = dataId.substring(0, dataId.indexOf('|'));
            List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();
            for (ModuleDeploymentCO module : applicationDeployment.getModules()) {
                String configDataType = module.getConfigObjectType();
                String uri = module.getUri();
                if (log.isDebugEnabled()) {
                    log.debug("Processing module " + uri + ", type " + configDataType);
                }
                ModuleInfoFactory factory = ModuleInfoFactory.getInstance(configDataType);
                if (factory == null) {
                    log.error("Unknown module type " + configDataType);
                    continue;
                }
                String deploymentDescriptorURI = factory.locateDeploymentDescriptor(config, baseURI + "/" + uri);
                if (log.isDebugEnabled()) {
                    log.debug("Loading deployment descriptor " + deploymentDescriptorURI);
                }
                moduleInfos.add(factory.create(uri, config.extract(deploymentDescriptorURI), loadTargetMappings(module)));
            }
            byte[] deploymentDescriptor;
            try {
                deploymentDescriptor = config.extract(baseURI + "/META-INF/application.xml");
            } catch (JMException ex) {
                if (ex.getCause() instanceof DocumentNotFoundException) {
                    deploymentDescriptor = null;
                } else {
                    throw ex;
                }
            }
            return new ApplicationInfo(deploymentDescriptor, loadTargetMappings(applicationDeployment),
                    moduleInfos.toArray(new ModuleInfo[moduleInfos.size()]));
        }
    }
    
    private TargetMapping[] loadTargetMappings(DeployedObjectCO object) throws JMException, ConnectorException, InterruptedException {
        List<TargetMapping> targetMappings = new ArrayList<TargetMapping>();
        for (DeploymentTargetMappingCO targetMapping : object.getTargetMappings()) {
            DeploymentTargetCO targetConfigObject = targetMapping.getTarget();
            Target target;
            if (targetConfigObject instanceof ClusteredTargetCO) {
                target = new ClusterTarget(((ClusteredTargetCO)targetConfigObject).getName());
            } else if (targetConfigObject instanceof ServerTargetCO) {
                ServerTargetCO serverTarget = (ServerTargetCO)targetConfigObject;
                target = new ServerTarget(serverTarget.getNodeName(), serverTarget.getName());
            } else {
                log.warn("Ignoring unexpected target configuration object type " + targetConfigObject.getConfigObjectType());
                target = null;
            }
            if (target != null) {
                targetMappings.add(new TargetMapping(target, targetMapping.getEnable()));
            }
        }
        return targetMappings.toArray(new TargetMapping[targetMappings.size()]);
    }

    @Override
    public int hashCode() {
        return applicationName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApplicationInfoQuery) {
            ApplicationInfoQuery other = (ApplicationInfoQuery)obj;
            return other.applicationName.equals(applicationName);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + applicationName + ")";
    }
}
