/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2014 Crossroads Bank for Social Security
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

import java.util.List;
import java.util.Map;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.config.types.CellCO;
import be.fgov.kszbcss.rhq.websphere.config.types.NodeCO;
import be.fgov.kszbcss.rhq.websphere.config.types.ServerCO;

/**
 * Facade that gives access to various parts of the cell configuration. This includes:
 * <ul>
 * <li>Access to the configuration service to query configuration objects.
 * <li>Access to the configuration repository to query configuration documents.
 * <li>Access to the <tt>AppManagement</tt> MBean to retrieve information about a deployed
 * application.
 * </ul>
 */
public interface Config {
    String getWebSphereVersion() throws JMException, ConnectorException, InterruptedException;
    <T extends ConfigObject> Path<T> path(Class<T> type, String name);
    <T extends ConfigObject> Path<T> path(Class<T> type);
    Path<CellCO> cell();
    Path<NodeCO> node(String nodeName);
    Path<ServerCO> server(String nodeName, String serverName);
    Path<Scope> allScopes(String nodeName, String serverName) throws JMException, ConnectorException, InterruptedException, ConfigQueryException;
    String[] listResourceNames(String parent, int type, int depth) throws JMException, ConnectorException;
    byte[] extract(String docURI) throws JMException, ConnectorException;
    Map<String,List<Map<String,String>>> getApplicationInfo(final String appName) throws JMException, ConnectorException, InterruptedException;
}
