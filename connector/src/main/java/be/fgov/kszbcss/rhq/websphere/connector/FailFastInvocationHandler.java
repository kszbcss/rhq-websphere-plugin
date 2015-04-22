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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorNotAvailableException;

public class FailFastInvocationHandler implements InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(FailFastInvocationHandler.class);
    
    private final AdminClient target;
    private long lastConnectorNotAvailableException = -1;
    
    public FailFastInvocationHandler(AdminClient target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (lastConnectorNotAvailableException != -1 && System.currentTimeMillis()-lastConnectorNotAvailableException > 120000) {
            log.debug("Resetting lastConnectorNotAvailableException");
            lastConnectorNotAvailableException = -1;
        }
        if (lastConnectorNotAvailableException == -1) {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException invocationTargetException) {
                Throwable exception = invocationTargetException.getCause();
                Throwable t = exception;
                do {
                    if (log.isDebugEnabled()) {
                        log.debug("cause = " + t.getClass().getName());
                    }
                    if (t instanceof ConnectorNotAvailableException) {
                        lastConnectorNotAvailableException = System.currentTimeMillis();
                        break;
                    }
                    t = t.getCause();
                } while (t != null);
                if (log.isDebugEnabled()) {
                    if (lastConnectorNotAvailableException == -1) {
                        log.debug("Not setting lastConnectorNotAvailableException");
                    } else {
                        log.debug("Setting lastConnectorNotAvailableException");
                    }
                }
                throw exception;
            }
        } else {
            throw new ConnectorNotAvailableException("The connector has been temporarily marked as unavailable because of a previous ConnectorNotAvailableException");
        }
    }
}
