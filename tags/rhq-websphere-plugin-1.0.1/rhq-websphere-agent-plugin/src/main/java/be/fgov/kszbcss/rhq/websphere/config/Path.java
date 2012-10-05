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
package be.fgov.kszbcss.rhq.websphere.config;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.exception.ConnectorException;

public abstract class Path {
    private static final Log log = LogFactory.getLog(Path.class);
    
    abstract ConfigObject[] resolveRelative(String relativePath) throws JMException, ConnectorException, InterruptedException;
    
    public final Path path(String type, String name) {
        return new RelativePath(this, type + "=" + name);
    }
    
    public final Path path(String type) {
        return path(type, "");
    }
    
    public ConfigObject[] resolve() throws JMException, ConnectorException, InterruptedException {
        ConfigObject[] configObjects = resolveRelative(null);
        if (log.isDebugEnabled()) {
            if (configObjects.length == 0) {
                log.debug("No configuration data found");
            } else {
                StringBuilder buffer = new StringBuilder("Configuration data found:");
                for (ConfigObject configObject : configObjects) {
                    buffer.append("\n * ");
                    buffer.append(configObject.getId());
                }
                log.debug(buffer.toString());
            }
        }
        return configObjects;
    }
    
    public ConfigObject resolveSingle() throws JMException, ConnectorException, InterruptedException {
        ConfigObject[] configObjects = resolve();
        if (configObjects.length == 1) {
            return configObjects[0];
        } else {
            // TODO: proper exception type
            throw new RuntimeException("More than one configuration object found");
        }
    }
}
