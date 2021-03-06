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
package com.ibm.websphere.management;

import java.util.Properties;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface AdminClient {
    String CONNECTOR_TYPE = "type";
    String CONNECTOR_HOST = "host";
    String CONNECTOR_PORT = "port";
    String CONNECTOR_TYPE_SOAP = "SOAP";
    String CONNECTOR_TYPE_RMI = "RMI";
    String CONNECTOR_SECURITY_ENABLED = "securityEnabled";
    String USERNAME = "username";
    String PASSWORD = "password";
    String CACHE_DISABLED = "cacheDisabled";

    String getType();
    
    Properties getConnectorProperties();
    
    ObjectName getServerMBean() throws ConnectorException;
    
    Session isAlive() throws ConnectorException;
    
    Session isAlive(int timeout) throws ConnectorException;
    
    Set<ObjectName> queryNames(ObjectName name, QueryExp query) throws ConnectorException;
    
    Integer getMBeanCount() throws ConnectorException;
    
    String getDomainName() throws ConnectorException;
    
    String getDefaultDomain() throws ConnectorException;
    
    MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException, ConnectorException;
    
    boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException, ConnectorException;
    
    boolean isRegistered(ObjectName name) throws ConnectorException;
    
    Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, ConnectorException;
    
    AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException, ConnectorException;
    
    void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, javax.management.MBeanException, ReflectionException, ConnectorException;
    
    AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException, ConnectorException;
    
    Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException, ConnectorException;
    
    void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ConnectorException;
    
    void addNotificationListenerExtended(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws ConnectorException;
    
    void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException, ConnectorException;
    
    @Deprecated
    void removeNotificationListenerExtended(NotificationListener listener) throws ListenerNotFoundException, ConnectorException;
    
    void removeNotificationListenerExtended(ObjectName name, NotificationListener listener) throws ListenerNotFoundException, ConnectorException;
    
    ClassLoader getClassLoaderFor(ObjectName name) throws ConnectorException, InstanceNotFoundException;
    
    ClassLoader getClassLoader(ObjectName name) throws ConnectorException, InstanceNotFoundException;
    
    Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) throws ConnectorException;
    
    ObjectInstance getObjectInstance(ObjectName objectName) throws ConnectorException, InstanceNotFoundException;
    
    void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws ConnectorException, InstanceNotFoundException;
    
    void removeNotificationListener(ObjectName name, ObjectName listener) throws ConnectorException, InstanceNotFoundException, ListenerNotFoundException;
    
    void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws ConnectorException, InstanceNotFoundException, ListenerNotFoundException;
}
