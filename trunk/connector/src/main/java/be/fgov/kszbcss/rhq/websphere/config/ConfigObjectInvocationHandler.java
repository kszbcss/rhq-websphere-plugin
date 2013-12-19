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
package be.fgov.kszbcss.rhq.websphere.config;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.proxy.AppManagement;
import be.fgov.kszbcss.rhq.websphere.proxy.ConfigService;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.configservice.ConfigServiceHelper;
import com.ibm.websphere.management.configservice.SystemAttributes;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Invocation handler for {@link ConfigObject} proxies. This class is designed to be thread safe
 * because {@link CellConfiguration} caches {@link ConfigObject} instances and they may therefore be
 * accessed concurrently.
 */
final class ConfigObjectInvocationHandler implements InvocationHandler, ConfigObject {
    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(ConfigObjectInvocationHandler.class);
    
    private final ConfigObjectTypeDesc type;
    private CellConfiguration config;
    private final ObjectName objectName;
    private AttributeList attributes;
    private Map<String,Object> references;

    ConfigObjectInvocationHandler(ConfigObjectTypeDesc type, CellConfiguration config, ObjectName objectName) {
        this.type = type;
        this.config = config;
        this.objectName = objectName;
    }

    public String getId() {
        return objectName.getKeyProperty(SystemAttributes._WEBSPHERE_CONFIG_DATA_ID);
    }
    
    public String getConfigObjectType() {
        return objectName.getKeyProperty(SystemAttributes._WEBSPHERE_CONFIG_DATA_TYPE);
    }
    
    public synchronized void detach() throws JMException, ConnectorException, InterruptedException {
        for (ConfigObjectAttributeDesc desc : type.getAttributeDescriptors()) {
            Object value = getAttributeValue(desc);
            if (desc.isReference()) {
                if (value instanceof List) {
                    for (Object item : (List<?>)value) {
                        ((ConfigObject)item).detach();
                    }
                } else {
                    ((ConfigObject)value).detach();
                }
            }
        }
        config = null;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass == ConfigObject.class || declaringClass == Object.class) {
            // TODO: not correct for Object#equals
            return method.invoke(this, args);
        } else {
            return getAttributeValue(type.getAttributeDescriptor(method));
        }
    }
    
    private synchronized Object getAttributeValue(ConfigObjectAttributeDesc desc) throws JMException, ConnectorException, InterruptedException {
        if (attributes == null) {
            if (log.isDebugEnabled()) {
                log.debug("Loading attributes for configuration object " + objectName + " ...");
            }
            final String[] attributeNames = type.getAttributeNames();
            attributes = config.execute(new SessionAction<AttributeList>() {
                public AttributeList execute(ConfigService configService, AppManagement appManagement, Session session) throws JMException, ConnectorException {
                    return configService.getAttributes(session, objectName, attributeNames, false);
                }
            });
            if (log.isDebugEnabled()) {
                log.debug("Result: " + attributes);
            }
        }
        String attributeName = desc.getName();
        if (desc.isReference()) {
            Object result;
            if (references == null) {
                references = new HashMap<String,Object>();
                result = null;
            } else {
                result = references.get(attributeName);
            }
            if (result == null) {
                Object value = ConfigServiceHelper.getAttributeValue(attributes, attributeName);
                if (desc.isCollection()) {
                    List<?> list = (List<?>)value;
                    List<ConfigObject> resultList = new ArrayList<ConfigObject>(list.size());
                    for (Object objectName : list) {
                        resultList.add(config.getConfigObject((ObjectName)objectName));
                    }
                    result = resultList;
                } else {
                    result = config.getConfigObject((ObjectName)value);
                }
                references.put(attributeName, result);
            }
            return result;
        } else {
            return ConfigServiceHelper.getAttributeValue(attributes, attributeName);
        }
    }
    
    private synchronized void writeObject(ObjectOutputStream stream) throws IOException {
        if (config != null) {
            throw new IllegalStateException("Configuration object has not been detached");
        }
        stream.defaultWriteObject();
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("(");
        boolean first = true;
        for (ConfigObjectAttributeDesc desc : type.getAttributeDescriptors()) {
            if (first) {
                first = false;
            } else {
                buffer.append(',');
            }
            buffer.append(desc.getName());
            buffer.append('=');
            try {
                buffer.append(getAttributeValue(desc));
            } catch (JMException ex) {
                buffer.append("#ERROR#");
            } catch (ConnectorException ex) {
                buffer.append("#ERROR#");
            } catch (InterruptedException ex) {
                Thread.interrupted();
            }
        }
        return buffer.toString();
    }
}
