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
package be.fgov.kszbcss.rhq.websphere.component.server;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.Config;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.ThreadPoolCO;
import be.fgov.kszbcss.rhq.websphere.config.types.ThreadPoolManagerCO;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Query to retrieve a <tt>ThreadPool</tt> configuration object.
 */
public class ThreadPoolQuery implements ConfigQuery<ThreadPoolCO> {
    private static final long serialVersionUID = 1L;
    
    private final String node;
    private final String server;
    private final String name;

    public ThreadPoolQuery(String node, String server, String name) {
        this.node = node;
        this.server = server;
        this.name = name;
    }

    public ThreadPoolCO execute(Config config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        return config.server(node, server).path(ThreadPoolManagerCO.class).path(ThreadPoolCO.class, name).resolveAtMostOne(true);
    }


    @Override
    public int hashCode() {
        return 31*31*node.hashCode() + 31*server.hashCode() + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ThreadPoolQuery) {
            ThreadPoolQuery other = (ThreadPoolQuery)obj;
            return other.node.equals(node) && other.server.equals(server) && other.name.equals(name);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + node + "," + server + "," + name + ")";
    }
}
