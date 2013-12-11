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
/**
 * Contains utility classes to make it easier to invoke WebSphere MBeans.
 * WebSphere and the MBeans it exposed have some particular features
 * that require special attention:
 * <ul>
 * <li>The MBeans exposed by the WebSphere server generally have an object name
 * with a key property containing the version number (including the fix pack level)
 * of the product. This means that the object name may change during an upgrade of
 * the server.
 * <li>Some object names also contain IDs referring to the WebSphere configuration
 * (see the <tt>mbeanIdentifier</tt> key property). This means that in general, it is not
 * feasible to get the object name of an MBean without doing a query first.
 * <li>The WebSphere admin client transparently reconnects to the server instance
 * after a restart. This means that an MBean object name may change without the
 * application code noticing it.
 * </ul>
 * Typical client programs are short running and use
 * {@link com.ibm.websphere.management.AdminClient#queryNames(javax.management.ObjectName, javax.management.QueryExp)}
 * to get the object name before invoking the MBean. This approach is too simplistic
 * for an RHQ plugin:
 * <ul>
 * <li>Doing a query before each MBean invocation would cause unnecessary overhead.
 * Indeed, although the object name may change between invocations, this is a rare
 * event (but for which the plugin must be prepared).
 * <li>Querying the MBean name once at plugin (or component) startup and cache it
 * will not work if the WebSphere server is down at this moment and only comes online
 * later.
 * </ul>
 * Therefore the code in this package uses a different strategy:
 * <ul>
 * <li>Object names are resolved lazily and cached for subsequent invocations.
 * <li>If an invocation fails with an {@link javax.management.InstanceNotFoundException},
 * the code will attempt to re-resolve the object name and retry the invocation.
 * </ul>
 */
package be.fgov.kszbcss.rhq.websphere.mbean;