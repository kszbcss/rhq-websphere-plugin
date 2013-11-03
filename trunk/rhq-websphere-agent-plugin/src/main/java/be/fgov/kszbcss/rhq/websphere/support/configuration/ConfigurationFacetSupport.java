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
package be.fgov.kszbcss.rhq.websphere.support.configuration;

import java.util.HashSet;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionSimple;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.util.exception.ThrowableUtil;

import be.fgov.kszbcss.rhq.websphere.component.WebSphereComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;

public class ConfigurationFacetSupport implements ConfigurationFacet {
    private final WebSphereComponent<?> component;
    private final MBeanClient mbean;
    private final boolean ignoreMissingMBean;
    
    public ConfigurationFacetSupport(WebSphereComponent<?> component, MBeanClient mbean, boolean ignoreMissingMBean) {
        this.component = component;
        this.mbean = mbean;
        this.ignoreMissingMBean = ignoreMissingMBean;
    }
    
    public ConfigurationFacetSupport(WebSphereComponent<?> component, MBeanClient mbean) {
        this(component, mbean, false);
    }

    public Configuration loadResourceConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        ConfigurationDefinition configurationDefinition = component.getResourceContext().getResourceType().getResourceConfigurationDefinition();
        Set<String> attributeNames = new HashSet<String>();
        for (PropertyDefinition property : configurationDefinition.getPropertyDefinitions().values()) {
            if (property instanceof PropertyDefinitionSimple) {
                attributeNames.add(property.getName());
            }
        }
        AttributeList attributes;
        try {
            attributes = mbean.getAttributes(attributeNames.toArray(new String[attributeNames.size()]));
        } catch (InstanceNotFoundException ex) {
            // In some cases, the MBean is created lazily; we then simply ignore the InstanceNotFoundException
            // and don't load any configuration
            if (ignoreMissingMBean) {
                // TODO: not sure if this is the right way; maybe RHQ will save a new configuration if we do this...
                return null;
            } else {
                throw ex;
            }
        }
        for (int i=0; i<attributes.size(); i++) {
            Attribute attribute = (Attribute)attributes.get(i);
            configuration.put(new PropertySimple(attribute.getName(), attribute.getValue()));
        }
        return configuration;
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
        ConfigurationDefinition configurationDefinition = component.getResourceContext().getResourceType().getResourceConfigurationDefinition();
        AttributeList attributes = new AttributeList();
        for (PropertySimple property : report.getConfiguration().getSimpleProperties().values()) {
            PropertyDefinitionSimple definition = configurationDefinition.getPropertyDefinitionSimple(property.getName());
            if (!definition.isReadOnly()) {
                Object value;
                switch (definition.getType()) {
                    case INTEGER:
                        value = Integer.valueOf(property.getIntegerValue());
                        break;
                    case LONG:
                        value = Long.valueOf(property.getLongValue());
                        break;
                    case BOOLEAN:
                        value = Boolean.valueOf(property.getBooleanValue());
                        break;
                    case FLOAT:
                        value = Float.valueOf(property.getFloatValue());
                        break;
                    case DOUBLE:
                        value = Double.valueOf(property.getDoubleValue());
                        break;
                    default:
                        value = property.getStringValue();
                        break;
                }
                attributes.add(new Attribute(property.getName(), value));
            }
        }
        try {
            mbean.setAttributes(attributes);
            report.setStatus(ConfigurationUpdateStatus.SUCCESS);
        } catch (Exception ex) {
            report.setErrorMessage(ThrowableUtil.getStackAsString(ex));
            report.setStatus(ConfigurationUpdateStatus.FAILURE);
        }
    }
}
