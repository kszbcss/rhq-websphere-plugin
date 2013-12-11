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

import java.util.Set;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.process.WebSphereServer;

import com.ibm.websphere.management.exception.ConnectorException;

public abstract class MBeanObjectNamePatternLocator implements MBeanLocator {
    private static final Log log = LogFactory.getLog(MBeanObjectNamePatternLocator.class.getName());
    
    private final boolean recursive;

    public MBeanObjectNamePatternLocator(boolean recursive) {
        this.recursive = recursive;
    }
    
    protected abstract ObjectName getPattern(WebSphereServer server) throws JMException, ConnectorException, InterruptedException;
    
    public final Set<ObjectName> queryNames(WebSphereServer server) throws JMException, ConnectorException, InterruptedException {
        ObjectName pattern = getPattern(server);
        ObjectName actualPattern;
        if (recursive || server.getProcessType().equals("ManagedProcess")) {
            actualPattern = pattern;
        } else {
            actualPattern = new ObjectName(pattern + ",cell=" + server.getCell() + ",node=" + server.getNode() + ",process=" + server.getServer());
        }
        if (log.isDebugEnabled()) {
            log.debug("Query names for pattern " + actualPattern);
        }
        return server.getAdminClient().queryNames(actualPattern, null);
    }

    
}
