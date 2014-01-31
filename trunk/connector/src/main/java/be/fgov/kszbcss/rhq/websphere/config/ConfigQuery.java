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

import java.io.Serializable;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.proxy.ConfigService;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Encapsulates a query for WebSphere configuration data. Implementations of this interface actually
 * play two roles:
 * <ol>
 * <li>They are used as cache keys. This means that every implementation must correctly implement
 * {@link Object#equals(Object)} and {@link Object#hashCode()}. In addition, they must be
 * serializable so that they can be stored in a persistent cache.
 * <li>They contain the logic to execute the query against a {@link ConfigService} instance.
 * </ol>
 * 
 * @param <T>
 *            the return type of the configuration data query
 */
public interface ConfigQuery<T extends Serializable> extends Serializable {
    // TODO: Allowing the method to throw JMException is suboptimal.
    //       The reason is that in some cases, an JMException thrown by the connector
    //       is actually constructed by deserializing an exception from the server. In
    //       that case, the stack trace is from the server as well and doesn't contain
    //       the location in the plug-in code where the exception occurred. By not
    //       declaring JMException we would force the code to catch it and to wrap it
    //       in another exception, the stack trace of which would contain additional
    //       information about the location in the plug-in code where the exception occurred.
    T execute(Config config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException;
}
