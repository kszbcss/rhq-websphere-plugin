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
package be.fgov.kszbcss.rhq.websphere.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;
import org.rhq.plugins.jmx.JMXComponent;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;

public abstract class WebSphereComponent<T extends ResourceComponent<?>> implements JMXComponent<T>, OperationFacet {
    private static final Log log = LogFactory.getLog(WebSphereComponent.class);
    
    private ResourceContext<T> resourceContext;

    public final void start(ResourceContext<T> context) throws InvalidPluginConfigurationException, Exception {
        this.resourceContext = context;
        start();
    }
    
    public final ResourceContext<T> getResourceContext() {
        return resourceContext;
    }

    protected abstract void start() throws InvalidPluginConfigurationException, Exception;
    
    public abstract ApplicationServer getServer();
    
    /**
     * Determine whether the resource corresponding to this component is still present in the
     * WebSphere configuration.
     * 
     * @return <code>true</code> if the resource is present in the WebSphere configuration,
     *         <code>false</code> otherwise
     * @throws Exception 
     */
    protected abstract boolean isConfigured() throws Exception;

    public final AvailabilityType getAvailability() {
        try {
            if (!isConfigured()) {
                log.debug("isConfigured=false => availability == DOWN");
                return AvailabilityType.DOWN;
            } else {
                log.debug("isConfigured=true; continue with checking the runtime state");
            }
        } catch (Exception ex) {
            log.debug("Caught exception thrown by isConfigured => availability == DOWN", ex);
            return AvailabilityType.DOWN;
        }
        return doGetAvailability();
    }
    
    protected abstract AvailabilityType doGetAvailability();
    
    public final OperationResult invokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("checkConfiguration")) {
            // TODO: This doesn't have the expected effect - in the GUI the following error message is shown: "There was an error looking up this operation's results."
            //       Also, the result is not saved in the database.
            OperationResult result = new OperationResult();
            Configuration results = result.getComplexResults();
            results.put(new PropertySimple("isConfigured", Boolean.valueOf(isConfigured())));
            return result;
        } else {
            return doInvokeOperation(name, parameters);
        }
    }
    
    protected OperationResult doInvokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        return null;
    }
}
