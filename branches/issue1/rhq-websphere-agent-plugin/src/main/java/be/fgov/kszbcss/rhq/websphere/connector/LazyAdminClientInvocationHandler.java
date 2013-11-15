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
package be.fgov.kszbcss.rhq.websphere.connector;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.management.exception.ConnectorNotAvailableException;

public class LazyAdminClientInvocationHandler implements InvocationHandler {
    private static final Log log = LogFactory.getLog(LazyAdminClientInvocationHandler.class);
    
    private final AdminClientProvider provider;
    private long lastAttempt = -1;
    private AdminClient target;
    
    public LazyAdminClientInvocationHandler(AdminClientProvider provider) {
        this.provider = provider;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        synchronized (this) {
            if (target == null) {
                long timestamp = System.currentTimeMillis();
                if (lastAttempt != -1 && timestamp-lastAttempt < 120000) {
                    throw new ConnectorNotAvailableException("The connector is not available because a recent attempt to create the connector failed");
                } else {
                    log.debug("Attempting to create AdminClient...");
                    try {
                        target = provider.createAdminClient();
                        log.debug("AdminClient successfully created");
                    } catch (ConnectorException ex) {
                        log.debug("AdminClient creation failed with ConnectorException", ex);
                        throw ex;
                    } catch (Throwable ex) {
                        log.debug("AdminClient creation failed with unexpected exception", ex);
                        throw new ConnectorNotAvailableException("An attempt to create the connector failed", ex);
                    } finally {
                        lastAttempt = timestamp;
                    }
                }
            }
        }
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
