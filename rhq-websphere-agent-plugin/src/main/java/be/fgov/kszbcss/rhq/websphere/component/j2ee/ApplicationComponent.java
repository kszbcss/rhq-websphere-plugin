/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2014 Crossroads Bank for Social Security
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

import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.event.EventContext;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;
import be.fgov.kszbcss.rhq.websphere.config.ConfigData;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.process.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.proxy.ApplicationManager;
import be.fgov.kszbcss.rhq.websphere.support.measurement.JMXAttributeGroupHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;

public class ApplicationComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet, OperationFacet {
    private MBeanClient mbean;
    private MeasurementFacetSupport measurementFacetSupport;
    private ApplicationManager applicationManager;
    private ConfigData<ApplicationInfo> applicationInfo;
    private ConfigData<ApplicationConfiguration> applicationConfiguration;
    
    @Override
    protected void doStart() {
        ApplicationServer server = getServer();
        ResourceContext<WebSphereServerComponent> context = getResourceContext();
        mbean = server.getMBeanClient("WebSphere:type=Application,name=" + context.getResourceKey() + ",*");
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("specVersion", new ApplicationSpecVersionMeasurementHandler(this));
        measurementFacetSupport.setDefaultHandler(new JMXAttributeGroupHandler(mbean));
        applicationManager = server.getMBeanClient("WebSphere:type=ApplicationManager,*").getProxy(ApplicationManager.class);
        applicationInfo = registerConfigQuery(new ApplicationInfoQuery(getApplicationName()));
        applicationConfiguration = registerConfigQuery(new ApplicationConfigurationQuery(getApplicationName()));
    }
    
    public String getApplicationName() {
        return getResourceContext().getResourceKey();
    }
    
	public ApplicationInfo getApplicationInfo() throws InterruptedException, ConfigQueryException {
        return applicationInfo.get();
    }
    
	public ApplicationConfiguration getConfiguration() throws InterruptedException, ConfigQueryException {
        return applicationConfiguration.get();
    }
    
    public void registerLogEventContext(String moduleName, EventContext context) {
        getParent().registerLogEventContext(getApplicationName(), moduleName, null, context);
    }
    
    public void unregisterLogEventContext(String moduleName) {
        getParent().unregisterLogEventContext(getApplicationName(), moduleName, null);
    }
    
    public void registerLogEventContext(String moduleName, String componentName, EventContext context) {
        getParent().registerLogEventContext(getApplicationName(), moduleName, componentName, context);
    }
    
    public void unregisterLogEventContext(String moduleName, String componentName) {
        getParent().unregisterLogEventContext(getApplicationName(), moduleName, componentName);
    }
    
    @Override
    protected boolean isConfigured() throws Exception {
		return getApplicationInfo() != null && getApplicationInfo().getTargetMapping(getServer()) != null;
    }

    @Override
	protected AvailabilityType doGetAvailability() {
        try {
            mbean.getAttribute("name");
            return AvailabilityType.UP;
        } catch (Exception ex) {
            return AvailabilityType.DOWN;
        }
    }

    @Override
	public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    @Override
	public OperationResult invokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("start")) {
            applicationManager.startApplication(getApplicationName());
        } else if (name.equals("stop")) {
            applicationManager.stopApplication(getApplicationName());
        }
        return null;
    }
}
