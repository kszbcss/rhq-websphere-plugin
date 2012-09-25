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
package be.fgov.kszbcss.rhq.websphere.config;

import java.util.ArrayList;
import java.util.List;

import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.ObjectName;

import be.fgov.kszbcss.rhq.websphere.proxy.AppManagement;
import be.fgov.kszbcss.rhq.websphere.proxy.ConfigService;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.configservice.ConfigServiceHelper;
import com.ibm.websphere.management.configservice.SystemAttributes;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Represents an object in the WebSphere configuration. This class provides a more convenient API
 * than {@link ConfigService}.
 */
public class ConfigObject {
    private final CellConfiguration config;
    private final ObjectName objectName;
    private AttributeList attributes;
    
    ConfigObject(CellConfiguration config, ObjectName objectName) {
        this.config = config;
        this.objectName = objectName;
    }
    
    public String getId() {
        return objectName.getKeyProperty(SystemAttributes._WEBSPHERE_CONFIG_DATA_ID);
    }
    
    public String getType() {
        return objectName.getKeyProperty(SystemAttributes._WEBSPHERE_CONFIG_DATA_TYPE);
    }
    
    private AttributeList getAttributes() throws JMException, ConnectorException, InterruptedException {
        if (attributes == null) {
            attributes = config.execute(new SessionAction<AttributeList>() {
                public AttributeList execute(ConfigService configService, AppManagement appManagement, Session session) throws JMException, ConnectorException {
                    return configService.getAttributes(session, objectName, null, false);
                }
            });
        }
        return attributes;
    }
    
    public Object getAttribute(String name) throws JMException, ConnectorException, InterruptedException {
        Object value = ConfigServiceHelper.getAttributeValue(getAttributes(), name);
        if (value instanceof ObjectName) {
            return new ConfigObject(config, (ObjectName)value);
        } else {
            return value;
        }
    }
    
    public List<ConfigObject> getChildren(String attributeName) throws JMException, ConnectorException, InterruptedException {
        List<ConfigObject> children = new ArrayList<ConfigObject>();
        for (ObjectName objectName : (List<ObjectName>)getAttribute(attributeName)) {
            children.add(new ConfigObject(config, objectName));
        }
        return children;
    }
}
