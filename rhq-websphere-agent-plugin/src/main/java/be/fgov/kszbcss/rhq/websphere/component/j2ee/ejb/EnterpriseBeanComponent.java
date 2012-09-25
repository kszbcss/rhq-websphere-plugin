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

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.component.j2ee.DeploymentConfigurationFacetSupport;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.J2EEComponent;

import com.ibm.websphere.pmi.PmiConstants;

public abstract class EnterpriseBeanComponent extends J2EEComponent<EJBModuleComponent> implements MeasurementFacet, ConfigurationFacet {
    private DeploymentConfigurationFacetSupport configurationFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        super.start();
        configurationFacetSupport = new DeploymentConfigurationFacetSupport(getModule().getApplication(), getModuleName(), getBeanName());
    }
    
    protected abstract EnterpriseBeanType getType();
    
    @Override
    protected final String getPMIModule() {
        return PmiConstants.BEAN_MODULE;
    }

    public String getBeanName() {
        return getResourceContext().getResourceKey();
    }
    
    public EJBModuleComponent getModule() {
        return getResourceContext().getParentResourceComponent();
    }
    
    public String getModuleName() {
        return getModule().getModuleName();
    }
    
    @Override
    protected boolean isConfigured(boolean immediate) throws Exception {
        return getModule().getBeanNames(getType(), immediate).contains(getBeanName());
    }

    protected AvailabilityType doGetAvailability() {
        // Same as for servlets: if the bean is configured, then it is expected to be available.
        return AvailabilityType.UP;
    }

    public final Configuration loadResourceConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        loadResourceConfiguration(configuration);
        return configuration;
    }
    
    // May be overridden in subclasses to add configurations specific to bean types
    protected void loadResourceConfiguration(Configuration configuration) throws Exception {
        configurationFacetSupport.loadResourceConfiguration(configuration);
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
    }
}
