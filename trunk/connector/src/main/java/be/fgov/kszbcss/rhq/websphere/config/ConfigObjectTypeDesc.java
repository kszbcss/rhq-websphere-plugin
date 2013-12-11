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

final class ConfigObjectTypeDesc implements Serializable {
    private final Class<? extends ConfigObject> iface;
    private final String name;
    private final Map<Method,ConfigObjectAttributeDesc> attributes = new HashMap<Method,ConfigObjectAttributeDesc>();
    
    ConfigObjectTypeDesc(Class<? extends ConfigObject> iface, String name) {
        this.iface = iface;
        this.name = name;
        for (Method method : iface.getMethods()) {
            if (method.getDeclaringClass().isAssignableFrom(ConfigObject.class)) {
                continue;
            }
            String methodName = method.getName();
            if (!methodName.startsWith("get")) {
                throw new IllegalArgumentException("Expected to find only getter methods in " + iface);
            }
            String attributeName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
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
    
    ConfigObject createInstance(CellConfiguration cellConfiguration, ObjectName objectName) {
        return (ConfigObject)Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] { iface }, new ConfigObjectInvocationHandler(this, cellConfiguration, objectName));
    }
    
    private Object writeReplace() throws ObjectStreamException {
        return new ConfigObjectTypeDescRef(iface);
    }
}
