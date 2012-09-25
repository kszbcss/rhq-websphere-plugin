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
package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiModuleConfig;
import com.ibm.websphere.pmi.stat.MBeanLevelSpec;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.WSStats;

public interface Perf {
    MBeanLevelSpec[] getInstrumentationLevel(MBeanStatDescriptor msd, Boolean recursive) throws JMException, ConnectorException;
    void setInstrumentationLevel(MBeanLevelSpec mbeanLevelSpec, Boolean recursive) throws JMException, ConnectorException;
    WSStats getStatsObject(MBeanStatDescriptor msd, Boolean recursive) throws JMException, ConnectorException;
    PmiModuleConfig[] getConfigs() throws JMException, ConnectorException;
    PmiModuleConfig getConfig(String objectName) throws JMException, ConnectorException;
}
