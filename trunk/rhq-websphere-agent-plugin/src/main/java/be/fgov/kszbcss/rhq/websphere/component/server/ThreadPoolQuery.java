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

import java.util.List;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.ThreadPoolCO;
import be.fgov.kszbcss.rhq.websphere.config.types.ThreadPoolManagerCO;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Query to retrieve the <tt>ThreadPool</tt> configuration objects from the
 * <tt>ThreadPoolManager</tt>.
 */
public class ThreadPoolQuery implements ConfigQuery<ThreadPoolConfiguration[]> {
    private static final long serialVersionUID = -7203291446803851440L;
    
    private final String node;
    private final String server;

    public ThreadPoolQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public ThreadPoolConfiguration[] execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        ThreadPoolManagerCO threadPoolManager = config.server(node, server).path(ThreadPoolManagerCO.class).resolveSingle();
        List<ThreadPoolCO> threadPools = threadPoolManager.getThreadPools();
        ThreadPoolConfiguration[] configs = new ThreadPoolConfiguration[threadPools.size()];
        int i = 0;
        for (ThreadPoolCO threadPool : threadPools) {
            configs[i++] = new ThreadPoolConfiguration(threadPool.getName() /*,
                    (String)ConfigServiceHelper.getAttributeValue(threadPool, SystemAttributes._WEBSPHERE_CONFIG_DATA_ID) */);
        }
        return configs;
    }


    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ThreadPoolQuery) {
            ThreadPoolQuery other = (ThreadPoolQuery)obj;
            return other.node.equals(node) && other.server.equals(server);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + node + "," + server + ")";
    }
}
