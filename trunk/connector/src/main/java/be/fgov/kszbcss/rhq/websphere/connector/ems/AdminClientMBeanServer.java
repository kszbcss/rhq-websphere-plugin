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
package be.fgov.kszbcss.rhq.websphere.connector.ems;

import java.io.ObjectInputStream;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;

import org.mc4j.ems.connection.EmsException;

import be.fgov.kszbcss.rhq.websphere.connector.ObjectNameMapper;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Adapts an {@link AdminClient} instance to the {@link MBeanServer} interface (as required by EMS).
 * The adapter will also take care of the routing properties (cell, node and process) added by
 * WebSphere, i.e. it will make sure that the application code only sees the "bare" object name.
 * This makes sure that existing RHQ plugins that are not WebSphere aware will work as expected.
 */
public class AdminClientMBeanServer implements MBeanServer {
    private final AdminClient adminClient;
    private final ObjectNameMapper mapper;

    public AdminClientMBeanServer(AdminClient adminClient) throws ConnectorException {
        this.adminClient = adminClient;
        ObjectName serverMBean = adminClient.getServerMBean();
        mapper = new ObjectNameMapper(serverMBean.getKeyProperty("cell"),
                serverMBean.getKeyProperty("node"), serverMBean.getKeyProperty("process"));
    }
    
    public void addNotificationListener(ObjectName name,
            NotificationListener listener, NotificationFilter filter,
            Object handback) throws InstanceNotFoundException {
        try {
            adminClient.addNotificationListener(mapper.toServerObjectName(name), listener, filter, handback);
        } catch (ConnectorException ex) {
            throw new EmsException(ex);
        }
    }

    public void addNotificationListener(ObjectName name, ObjectName listener,
            NotificationFilter filter, Object handback)
            throws InstanceNotFoundException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public ObjectInstance createMBean(String className, ObjectName name)
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException {
        throw new UnsupportedOperationException("createMBean is not supported");
    }

    public ObjectInstance createMBean(String className, ObjectName name,
            ObjectName loaderName) throws ReflectionException,
            InstanceAlreadyExistsException, MBeanRegistrationException,
            MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException {
        throw new UnsupportedOperationException("createMBean is not supported");
    }

    public ObjectInstance createMBean(String className, ObjectName name,
            Object[] params, String[] signature) throws ReflectionException,
            InstanceAlreadyExistsException, MBeanRegistrationException,
            MBeanException, NotCompliantMBeanException {
        throw new UnsupportedOperationException("createMBean is not supported");
    }

    public ObjectInstance createMBean(String className, ObjectName name,
            ObjectName loaderName, Object[] params, String[] signature)
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException, InstanceNotFoundException {
        throw new UnsupportedOperationException("createMBean is not supported");
    }

    public ObjectInputStream deserialize(ObjectName name, byte[] data)
            throws InstanceNotFoundException, OperationsException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public ObjectInputStream deserialize(String className, byte[] data)
            throws OperationsException, ReflectionException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public ObjectInputStream deserialize(String className,
            ObjectName loaderName, byte[] data)
            throws InstanceNotFoundException, OperationsException,
            ReflectionException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(ObjectName name, String attribute)
            throws MBeanException, AttributeNotFoundException,
            InstanceNotFoundException, ReflectionException {
        try {
            return adminClient.getAttribute(mapper.toServerObjectName(name), attribute);
        } catch (ConnectorException ex) {
            throw new EmsException(ex);
        }
    }

    public AttributeList getAttributes(ObjectName name, String[] attributes)
            throws InstanceNotFoundException, ReflectionException {
        try {
            return adminClient.getAttributes(mapper.toServerObjectName(name), attributes);
        } catch (ConnectorException ex) {
            throw new EmsException(ex);
        }
    }

    public ClassLoader getClassLoader(ObjectName loaderName)
            throws InstanceNotFoundException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public ClassLoader getClassLoaderFor(ObjectName mbeanName)
            throws InstanceNotFoundException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public ClassLoaderRepository getClassLoaderRepository() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getDefaultDomain() {
        try {
            return adminClient.getDefaultDomain();
        } catch (ConnectorException ex) {
            throw new EmsException(ex);
        }
    }

    public String[] getDomains() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Integer getMBeanCount() {
        try {
            return adminClient.getMBeanCount();
        } catch (ConnectorException ex) {
            throw new EmsException(ex);
        }
    }

    public MBeanInfo getMBeanInfo(ObjectName name)
            throws InstanceNotFoundException, IntrospectionException,
            ReflectionException {
        try {
            return adminClient.getMBeanInfo(mapper.toServerObjectName(name));
        } catch (ConnectorException ex) {
            throw new EmsException(ex);
        }
    }

    public ObjectInstance getObjectInstance(ObjectName name)
            throws InstanceNotFoundException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Object instantiate(String className) throws ReflectionException,
            MBeanException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Object instantiate(String className, ObjectName loaderName)
            throws ReflectionException, MBeanException,
            InstanceNotFoundException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Object instantiate(String className, Object[] params,
            String[] signature) throws ReflectionException, MBeanException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Object instantiate(String className, ObjectName loaderName,
            Object[] params, String[] signature) throws ReflectionException,
            MBeanException, InstanceNotFoundException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Object invoke(ObjectName name, String operationName,
            Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException,
            ReflectionException {
        try {
            return adminClient.invoke(mapper.toServerObjectName(name), operationName, params, signature);
        } catch (ConnectorException ex) {
            throw new EmsException(ex);
        }
    }

    public boolean isInstanceOf(ObjectName name, String className)
            throws InstanceNotFoundException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public boolean isRegistered(ObjectName name) {
        try {
            return adminClient.isRegistered(mapper.toServerObjectName(name));
        } catch (ConnectorException ex) {
            throw new EmsException(ex);
        }
    }

    public Set queryMBeans(ObjectName name, QueryExp query) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Set queryNames(ObjectName name, QueryExp query) {
        try {
            return mapper.toClientObjectNames(adminClient.queryNames(mapper.toServerObjectName(name), query));
        } catch (ConnectorException ex) {
            throw new EmsException(ex);
        }
    }

    public ObjectInstance registerMBean(Object object, ObjectName name)
            throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void removeNotificationListener(ObjectName name, ObjectName listener)
            throws InstanceNotFoundException, ListenerNotFoundException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void removeNotificationListener(ObjectName name,
            NotificationListener listener) throws InstanceNotFoundException,
            ListenerNotFoundException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void removeNotificationListener(ObjectName name,
            ObjectName listener, NotificationFilter filter, Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void removeNotificationListener(ObjectName name,
            NotificationListener listener, NotificationFilter filter,
            Object handback) throws InstanceNotFoundException,
            ListenerNotFoundException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void setAttribute(ObjectName name, Attribute attribute)
            throws InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException {
        try {
            adminClient.setAttribute(mapper.toServerObjectName(name), attribute);
        } catch (ConnectorException ex) {
            throw new EmsException(ex);
        }
    }

    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
            throws InstanceNotFoundException, ReflectionException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void unregisterMBean(ObjectName name)
            throws InstanceNotFoundException, MBeanRegistrationException {
        // TODO
        throw new UnsupportedOperationException();
    }
}
