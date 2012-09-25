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
package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.SIBDestination;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.SIBDestinationMap;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.SIBDestinationMapQuery;
import be.fgov.kszbcss.rhq.websphere.proxy.J2CMessageEndpoint;

import com.ibm.websphere.pmi.PmiConstants;

public class MessageDrivenBeanComponent extends EnterpriseBeanComponent {
    private static final Log log = LogFactory.getLog(MessageDrivenBeanComponent.class);
    
    private J2CMessageEndpoint endpoint;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        super.start();
        ResourceContext<EJBModuleComponent> context = getResourceContext();
        ModuleComponent parent = context.getParentResourceComponent();
        endpoint = getServer().getMBeanClient("WebSphere:type=J2CMessageEndpoint,name="
                + parent.getApplicationName() + "#" + parent.getModuleName() + "#"
                + context.getResourceKey() + "_J2CMessageEndpoint,*").getProxy(J2CMessageEndpoint.class);
    }

    @Override
    protected EnterpriseBeanType getType() {
        return EnterpriseBeanType.MESSAGE_DRIVEN;
    }

    @Override
    protected String getPMISubmodule() {
        return PmiConstants.EJB_MESSAGEDRIVEN;
    }

    @Override
    protected AvailabilityType doGetAvailability() {
        AvailabilityType availability = super.doGetAvailability();
        if (availability == AvailabilityType.DOWN) {
            return AvailabilityType.DOWN;
        } else {
            try {
                Integer status = endpoint.getStatus();
                if (log.isDebugEnabled()) {
                    log.debug("Status of J2CMessageEndpoint for MDB " + getResourceContext().getResourceKey() + ": " + status);
                }
                return status != null && status.intValue() == 1 ? AvailabilityType.UP : AvailabilityType.DOWN;
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Error getting status of J2CMessageEndpoint for MDB " + getResourceContext().getResourceKey(), ex);
                }
                return AvailabilityType.DOWN;
            }
        }
    }

    @Override
    public void loadResourceConfiguration(Configuration configuration) throws Exception {
        super.loadResourceConfiguration(configuration);
        ApplicationServer server = getServer();
        List<Map<String,String>> data = getModule().getApplication().getConfiguration(true).getData("BindJndiForEJBMessageBinding", getModuleName(), getBeanName());
        if (data == null || data.size() != 1) {
            throw new Exception("No message listener binding found");
        }
        Map<String,String> binding = data.get(0);
        String activationSpecJndiName = binding.get("JNDI");
        String destinationJndiName = binding.get("jndi.dest");
        ActivationSpecInfo activationSpec = server.queryConfig(new ActivationSpecQuery(server.getNode(), server.getServer()), true).getActivationSpec(activationSpecJndiName);
        String busName = null;
        String destinationName = null;
        if (destinationJndiName != null && destinationJndiName.length() == 0) {
            destinationJndiName = null;
        }
        if (destinationJndiName == null && activationSpec != null) {
            // In this case, the destination is specified in the activation spec
            destinationJndiName = activationSpec.getDestinationJndiName();
            if (destinationJndiName == null) {
                // This case occurs e.g. for activation specs created automatically for SCA modules in WPS
                Map<String,Object> properties = activationSpec.getProperties();
                busName = (String)properties.get("busName");
                destinationName = (String)properties.get("destinationName");
            }
        }
        if (destinationJndiName != null) {
            SIBDestinationMap sibDestinationMap = server.queryConfig(new SIBDestinationMapQuery(server.getNode(), server.getServer()), true);
            SIBDestination dest = sibDestinationMap.getSIBDestination(destinationJndiName);
            if (dest != null) {
                busName = dest.getBusName();
                destinationName = dest.getDestinationName();
            }
        }
        configuration.put(new PropertySimple("activationSpecJndiName", activationSpecJndiName));
        configuration.put(new PropertySimple("destinationJndiName", destinationJndiName));
        configuration.put(new PropertySimple("busName", busName));
        configuration.put(new PropertySimple("destinationName", destinationName));
        if (activationSpec != null) {
            Map<String,Object> properties = activationSpec.getProperties();
            configuration.put(new PropertySimple("maxConcurrency", properties.get("maxConcurrency")));
        }
    }

    protected OperationResult doInvokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("pause")) {
            endpoint.pause();
        } else if (name.equals("resume")) {
            endpoint.resume();
        }
        return null;
    }
}
