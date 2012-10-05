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

import be.fgov.kszbcss.rhq.websphere.proxy.AppManagement;
import be.fgov.kszbcss.rhq.websphere.proxy.ConfigService;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * An action that is executed in the context of a {@link Session}. This applies to methods of the
 * <tt>ConfigService</tt> and <tt>AppManagement</tt> MBeans. Note that the workspace ID used by the
 * <tt>AppManagement</tt> API can be derived from the {@link Session} object by calling
 * {@link Session#toString()} (it is the concatenation of the user name and session ID).
 * 
 * @param <T>
 *            the result type for the action
 */
interface SessionAction<T> {
    T execute(ConfigService configService, AppManagement appManagement, Session session) throws JMException, ConnectorException;
}