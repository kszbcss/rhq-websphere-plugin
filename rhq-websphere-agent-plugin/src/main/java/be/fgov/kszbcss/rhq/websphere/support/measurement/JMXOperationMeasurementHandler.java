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
package be.fgov.kszbcss.rhq.websphere.support.measurement;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.process.WebSphereServer;

public class JMXOperationMeasurementHandler implements MeasurementHandler {
    private static final Logger log = LoggerFactory.getLogger(JMXOperationMeasurementHandler.class);
    
    private final MBeanClient mbean;
    private final String operationName;
    private final boolean ignoreInstanceNotFound;

    public JMXOperationMeasurementHandler(MBeanClient mbean, String operationName, boolean ignoreInstanceNotFound) {
        this.mbean = mbean;
        this.operationName = operationName;
        this.ignoreInstanceNotFound = ignoreInstanceNotFound;
    }

    public void getValue(WebSphereServer server, MeasurementReport report, MeasurementScheduleRequest request) throws InterruptedException, JMException, ConnectorException {
        Object value;
        try {
            value = mbean.invoke(operationName, new Object[0], new String[0]);
        } catch (InstanceNotFoundException ex) {
            if (ignoreInstanceNotFound) {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring InstanceNotFoundException for measurement of " + request.getName() + " on " + mbean);
                }
                value = null;
            } else {
                throw ex;
            }
        }
        JMXMeasurementDataUtils.addData(report, request, value);
    }
}
