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
package be.fgov.kszbcss.rhq.websphere.component.j2ee.web;

import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ApplicationComponent;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.DeploymentConfigurationFacetSupport;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;

import com.ibm.websphere.management.exception.ConnectorException;

public class WebModuleComponent extends ModuleComponent implements ConfigurationFacet {
    private DeploymentConfigurationFacetSupport configurationFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        super.start();
        configurationFacetSupport = new DeploymentConfigurationFacetSupport(getApplication(), getModuleName(), null);
        ResourceContext<ApplicationComponent> context = getResourceContext();
        PropertySimple suppressLogEventsProp = context.getPluginConfiguration().getSimple("suppressLogEvents");
        boolean suppressLogEvents = suppressLogEventsProp != null && Boolean.TRUE.equals(suppressLogEventsProp.getBooleanValue());
        getApplication().registerLogEventContext(getModuleName(), suppressLogEvents ? null : context.getEventContext());
    }

    @Override
    protected String getMBeanType() {
        return "WebModule";
    }
    
    public Set<String> getServletNames() throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        Set<String> result = new HashSet<String>();
        for (Element servlet : Utils.getElements(getModuleInfo().getDeploymentDescriptor().getDocumentElement(), "servlet")) {
            result.add(Utils.getFirstElement(servlet, "servlet-name").getTextContent());
        }
        return result;
    }

    public Configuration loadResourceConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        configurationFacetSupport.loadResourceConfiguration(configuration);
        return configuration;
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
    }

    @Override
    public void stop() {
        getApplication().unregisterLogEventContext(getModuleName());
        super.stop();
    }
}
