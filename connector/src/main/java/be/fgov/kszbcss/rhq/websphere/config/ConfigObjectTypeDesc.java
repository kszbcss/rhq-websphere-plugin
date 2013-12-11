/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2013 Crossroads Bank for Social Security
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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import com.ibm.websphere.management.configservice.SystemAttributes;

final class ConfigObjectTypeDesc implements Serializable {
    private final Class<? extends ConfigObject> iface;
    private final String name;
    private final Map<Method,ConfigObjectAttributeDesc> attributes = new HashMap<Method,ConfigObjectAttributeDesc>();
    private final List<ConfigObjectTypeDesc> extensions = new ArrayList<ConfigObjectTypeDesc>();
    
    ConfigObjectTypeDesc(Class<? extends ConfigObject> iface) {
        this.iface = iface;
        ConfigObjectType ann = iface.getAnnotation(ConfigObjectType.class);
        name = ann.name();
        for (Method method : iface.getMethods()) {
            if (method.getDeclaringClass().isAssignableFrom(ConfigObject.class)) {
                continue;
            }
            String name = method.getName();
            if (!name.startsWith("get")) {
                throw new IllegalArgumentException("Expected to find only getter methods in " + iface);
            }
            String attributeName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
            Type returnType = method.getGenericReturnType();
            boolean collection;
            Class<?> attributeType;
            if (returnType instanceof ParameterizedType) {
                ParameterizedType t = (ParameterizedType)returnType;
                if (t.getRawType() == List.class) {
                    collection = true;
                    attributeType = (Class<?>)t.getActualTypeArguments()[0];
                } else {
                    // TODO: proper exception
                    throw new RuntimeException();
                }
            } else {
                collection = false;
                attributeType = (Class<?>)returnType;
            }
            attributes.put(method, new ConfigObjectAttributeDesc(attributeName, attributeType, collection));
        }
        for (Class<? extends ConfigObject> extension : ann.extensions()) {
            extensions.add(ConfigObjectTypeRegistry.getDescriptor(extension));
        }
    }
    
    String getName() {
        return name;
    }
    
    String[] getAttributeNames() {
        List<String> attributeNames = new ArrayList<String>(attributes.size());
        for (ConfigObjectAttributeDesc attribute : attributes.values()) {
            attributeNames.add(attribute.getName());
        }
        return attributeNames.toArray(new String[attributeNames.size()]);
    }
    
    ConfigObjectAttributeDesc[] getAttributeDescriptors() {
        return attributes.values().toArray(new ConfigObjectAttributeDesc[attributes.size()]);
    }
    
    ConfigObjectAttributeDesc getAttributeDescriptor(Method method) {
        return attributes.get(method);
    }
    
    private ConfigObjectTypeDesc findExtension(String type) {
        for (ConfigObjectTypeDesc extension : extensions) {
            if (extension.getName().equals(type)) {
                return extension;
            } else {
                ConfigObjectTypeDesc desc = extension.findExtension(type);
                if (desc != null) {
                    return desc;
                }
            }
        }
        return null;
    }
    
    ConfigObject createInstance(CellConfiguration cellConfiguration, ObjectName objectName) {
        String type = objectName.getKeyProperty(SystemAttributes._WEBSPHERE_CONFIG_DATA_TYPE);
        ConfigObjectTypeDesc desc;
        if (type.equals(name)) {
            desc = this;
        } else {
            desc = findExtension(type);
            if (desc == null) {
                // TODO: log this
                desc = this;
            }
        }
        return (ConfigObject)Proxy.newProxyInstance(desc.iface.getClassLoader(), new Class<?>[] { desc.iface }, new ConfigObjectInvocationHandler(desc, cellConfiguration, objectName));
    }
    
    private Object writeReplace() throws ObjectStreamException {
        return new ConfigObjectTypeDescRef(iface);
    }
}
