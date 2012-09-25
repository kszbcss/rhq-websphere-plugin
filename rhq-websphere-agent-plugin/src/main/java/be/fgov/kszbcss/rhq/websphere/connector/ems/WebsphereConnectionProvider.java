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

import javax.management.MBeanServer;

import org.mc4j.ems.impl.jmx.connection.support.providers.AbstractConnectionProvider;

import com.ibm.websphere.management.AdminClient;

public class WebsphereConnectionProvider extends AbstractConnectionProvider {
    private final AdminClient adminClient;
    private MBeanServerStatsProxy proxy;
    private MBeanServer mbeanServer;

    public WebsphereConnectionProvider(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    protected void doConnect() throws Exception {
        proxy = new MBeanServerStatsProxy(new AdminClientMBeanServer(adminClient));
        mbeanServer = proxy.buildServerProxy();
    }

    public MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    public long getRoundTrips() {
        return proxy.getRoundTrips();
    }

    public long getFailures() {
        return proxy.getFailures();
    }
}
