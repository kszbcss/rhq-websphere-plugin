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

import java.util.Set;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.event.EventContext;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;

public abstract class ModuleComponent extends WebSphereServiceComponent<ApplicationComponent> implements MeasurementFacet {
    private MBeanClient mbean;
    private MeasurementFacetSupport measurementFacetSupport;
    
    protected abstract String getMBeanType();
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        mbean = getServer().getMBeanClient("WebSphere:type=" + getMBeanType() + ",Application=" + getApplicationName() + ",name=" + getModuleName() + ",*");
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("specVersion", new ModuleSpecVersionMeasurementHandler(this));
    }

    public ApplicationComponent getApplication() {
        return getResourceContext().getParentResourceComponent();
    }
    
    public String getApplicationName() {
        return getApplication().getApplicationName();
    }
    
    public String getModuleName() {
        return getResourceContext().getResourceKey();
    }
    
    public void registerLogEventContext(String componentName, EventContext context) {
        getApplication().registerLogEventContext(getModuleName(), componentName, context);
    }
    
    public void unregisterLogEventContext(String componentName) {
        getApplication().unregisterLogEventContext(getModuleName(), componentName);
    }
    
    public ModuleInfo getModuleInfo(boolean immediate) throws InterruptedException, ConnectorException {
        return getApplication().getApplicationInfo(immediate).getModule(getModuleName());
    }
    
    @Override
    protected boolean isConfigured(boolean immediate) throws Exception {
        return getModuleInfo(immediate) != null;
    }

    protected AvailabilityType doGetAvailability() {
        try {
            mbean.getAttribute("name");
            return AvailabilityType.UP;
        } catch (Exception ex) {
            return AvailabilityType.DOWN;
        }
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public void stop() {
    }
}
