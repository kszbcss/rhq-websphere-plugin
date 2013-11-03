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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;

import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.StatsProxy;

public class MBeanServerStatsProxy implements InvocationHandler, StatsProxy {
    private final AtomicLong failures = new AtomicLong();
    private final AtomicLong roundTrips = new AtomicLong();
    private final MBeanServer target;
    
    public MBeanServerStatsProxy(MBeanServer target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean failure = true;
        try {
            Object result = method.invoke(target, args);
            failure = false;
            return result;
        } finally {
            if (failure) {
                failures.incrementAndGet();
            }
            roundTrips.incrementAndGet();
        }
    }

    public MBeanServer buildServerProxy() {
        return (MBeanServer)Proxy.newProxyInstance(MBeanServerStatsProxy.class.getClassLoader(),
                new Class<?>[] { MBeanServer.class }, this);
    }

    public long getFailures() {
        return failures.get();
    }

    public long getRoundTrips() {
        return roundTrips.get();
    }
}
