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
package be.fgov.kszbcss.rhq.websphere.component.server;

import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.component.ThreadPoolPMIMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.config.types.ThreadPoolCO;
import be.fgov.kszbcss.rhq.websphere.proxy.ThreadMonitor;
import be.fgov.kszbcss.rhq.websphere.support.configuration.ConfigurationFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;

import com.ibm.websphere.pmi.PmiConstants;

public class ThreadPoolComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet, ConfigurationFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    private ConfigurationFacetSupport configurationFacetSupport;
    private ThreadMonitor threadMonitor;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<WebSphereServerComponent> context = getResourceContext();
        ApplicationServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(this);
        String name = context.getResourceKey();
        // We don't use the ThreadPool mbean here because for some thread pools, no MBean is created
        // by the server (see the design document for more details).
        measurementFacetSupport.addHandler("stats", new ThreadPoolPMIMeasurementHandler(server.getServerMBean(), PmiConstants.THREADPOOL_MODULE, name));
        configurationFacetSupport = new ConfigurationFacetSupport(this,
                server.getMBeanClient("WebSphere:type=ThreadPool,name=" + name + ",*"), true);
        threadMonitor = server.getMBeanClient("XM4WAS:type=ThreadMonitor,*").getProxy(ThreadMonitor.class); 
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public Configuration loadResourceConfiguration() throws Exception {
        return configurationFacetSupport.loadResourceConfiguration();
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
        configurationFacetSupport.updateResourceConfiguration(report);
    }

    @Override
    protected boolean isConfigured() throws Exception {
        ApplicationServer server = getServer();
        for (ThreadPoolCO threadPool : server.queryConfig(new ThreadPoolManagerQuery(server.getNode(), server.getServer())).getThreadPools()) {
            if (threadPool.getName().equals(getResourceContext().getResourceKey())) {
                return true;
            }
        }
        return false;
    }

    protected AvailabilityType doGetAvailability() {
        // TODO Auto-generated method stub
        return AvailabilityType.UP;
    }

    @Override
    protected OperationResult doInvokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("dump")) {
            threadMonitor.dumpThreads(getResourceContext().getResourceKey(), true,
                    Boolean.valueOf(parameters.getSimpleValue("shorten")));
            return null;
        } else {
            return super.doInvokeOperation(name, parameters);
        }
    }

    public void stop() {
    }
}
