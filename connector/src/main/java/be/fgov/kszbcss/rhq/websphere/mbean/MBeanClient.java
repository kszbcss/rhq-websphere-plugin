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
package be.fgov.kszbcss.rhq.websphere.mbean;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.fgov.kszbcss.rhq.websphere.process.WebSphereServer;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * An MBean identified by an object name pattern. An instance of this class lazily resolves
 * (completes) the object name and also handles the case where the object name changes (e.g. after a
 * server upgrade).
 */
public class MBeanClient {
    private static final Logger log = LoggerFactory.getLogger(MBeanClient.class);
    
    private interface Action<T> {
        public T execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException;
    }
    
    private final WebSphereServer server;
    private final MBeanLocator locator;
    private final Map<Class<?>,Object> proxies = new HashMap<Class<?>,Object>();
    private ObjectName cachedObjectName;
    
    MBeanClient(WebSphereServer server, MBeanLocator locator) {
        this.server = server;
        this.locator = locator;
    }
    
    public MBeanLocator getLocator() {
        return locator;
    }

    public ObjectName getObjectName(boolean refresh) throws JMException, ConnectorException, InterruptedException {
        if (refresh) {
            ObjectName objectName = internalGetObjectName();
            synchronized (this) {
                cachedObjectName = objectName;
            }
            return objectName;
        } else {
            synchronized (this) {
                if (cachedObjectName == null) {
                    cachedObjectName = internalGetObjectName();
                }
                return cachedObjectName;
            }
        }
    }
    
    private ObjectName internalGetObjectName() throws JMException, ConnectorException, InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to resolve " + locator);
        }
        Set<ObjectName> objectNames = locator.queryNames(server);
        if (log.isDebugEnabled()) {
            log.debug("Result: " + objectNames);
        }
        int size = objectNames.size();
        if (size == 1) {
            return objectNames.iterator().next();
        } else {
            throw new InstanceNotFoundException((size == 0 ? "No MBean" : "Mutiple MBeans") + " found for locator " + locator
                    + " (process=" + server.getServer() + ", node=" + server.getNode() + ", cell=" + server.getCell() + ")");
        }
    }
    
    public <T> T getProxy(Class<T> iface) {
        synchronized (proxies) {
            Object proxy = proxies.get(iface);
            if (proxy == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating dynamic proxy for MBean " + locator);
                }
                for (Method method : iface.getMethods()) {
                    if (!throwsException(method, JMException.class) || !throwsException(method, ConnectorException.class)) {
                        throw new IllegalArgumentException(iface.getName() + " is not a valid proxy class: method " + method.getName()
                                + " must declare JMException and ConnectorException");
                    }
                }
                proxy = Proxy.newProxyInstance(MBeanClient.class.getClassLoader(), new Class<?>[] { iface, MBeanClientProxy.class },
                        new MBeanClientInvocationHandler(this));
                proxies.put(iface, proxy);
            }
            return iface.cast(proxy);
        }
    }
    
    private static boolean throwsException(Method method, Class<?> exceptionType) {
        for (Class<?> candidate : method.getExceptionTypes()) {
            if (candidate.isAssignableFrom(exceptionType)) {
                return true;
            }
        }
        return false;
    }
    
    private <T> T execute(Action<T> action) throws JMException, ConnectorException, InterruptedException {
        AdminClient adminClient = server.getAdminClient();
        ObjectName cachedObjectName;
        synchronized (this) {
            cachedObjectName = this.cachedObjectName;
        }
        if (cachedObjectName != null) {
            try {
                return action.execute(adminClient, cachedObjectName);
            } catch (InstanceNotFoundException ex) {
                // Continue; we will attempt to re-resolve the object name
            }
        }
        cachedObjectName = internalGetObjectName();
        synchronized (this) {
            this.cachedObjectName = cachedObjectName;
        }
        if (log.isDebugEnabled()) {
            log.debug("Found MBean instance: " + cachedObjectName);
        }
        return action.execute(adminClient, cachedObjectName);
    }
    
    public Object invoke(final String operationName, final Object[] params, final String[] signature) throws JMException, ConnectorException, InterruptedException {
        return execute(new Action<Object>() {
            public Object execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException {
                return adminClient.invoke(objectName, operationName, params, signature);
            }
        });
    }
    
    public Object getAttribute(final String attribute) throws JMException, ConnectorException, InterruptedException {
        return execute(new Action<Object>() {
            public Object execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException {
                return adminClient.getAttribute(objectName, attribute);
            }
        });
    }
    
    public AttributeList getAttributes(final String[] attributes) throws JMException, ConnectorException, InterruptedException {
        return execute(new Action<AttributeList>() {
            public AttributeList execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException {
                return adminClient.getAttributes(objectName, attributes);
            }
        });
    }
    
    public AttributeList setAttributes(final AttributeList attributes) throws JMException, ConnectorException, InterruptedException {
        return execute(new Action<AttributeList>() {
            public AttributeList execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException {
                return adminClient.setAttributes(objectName, attributes);
            }
        });
    }
    
    /**
     * Check whether an MBean matching this client's {@link MBeanLocator} is registered in the MBean
     * server.
     * 
     * @return <code>true</code> if the MBean is registered, <code>false</code> otherwise
     * @throws JMException
     * @throws ConnectorException
     * @throws InterruptedException 
     */
    public boolean isRegistered() throws JMException, ConnectorException, InterruptedException {
        try {
            execute(new Action<Void>() {
                public Void execute(AdminClient adminClient, ObjectName objectName) throws JMException, ConnectorException {
                    // We need to take into account the case where the MBean has been re-registered
                    // with an object name that is different than the last known object name,
                    // but that still matches the MBeanLocator. To achieve this, we throw an
                    // InstanceNotFoundException if the MBean is not registered. The execute method
                    // will then attempt to re-resolve the object name.
                    if (!adminClient.isRegistered(objectName)) {
                        throw new InstanceNotFoundException();
                    }
                    return null;
                }
            });
            return true;
        } catch (InstanceNotFoundException ex) {
            return false;
        }
    }
}
