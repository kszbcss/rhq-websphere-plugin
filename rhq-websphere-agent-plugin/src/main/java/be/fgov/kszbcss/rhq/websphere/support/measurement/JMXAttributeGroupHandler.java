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

import java.util.Arrays;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.process.WebSphereServer;

public class JMXAttributeGroupHandler implements MeasurementGroupHandler {
    private static final Logger log = LoggerFactory.getLogger(JMXAttributeGroupHandler.class);
    
    private final MBeanClient mbean;
    
    public JMXAttributeGroupHandler(MBeanClient mbean) {
        this.mbean = mbean;
    }
    
    public void getValues(WebSphereServer server, MeasurementReport report, Map<String,MeasurementScheduleRequest> requests) {
        String[] attributes = requests.keySet().toArray(new String[requests.size()]);
        AttributeList list;
        try {
            list = mbean.getAttributes(attributes);
        } catch (Exception ex) {
            log.error("Failed to get values for attributes " + requests.keySet());
            // TODO: shouldn't we fall back to fetching each attribute individually?
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Fetched attributes from " + mbean + ": " + Arrays.asList(attributes) + "=" + list);
        }
        for (int i=0; i<attributes.length; i++) {
            JMXMeasurementDataUtils.addData(report, requests.get(attributes[i]), ((Attribute)list.get(i)).getValue());
        }
    }
}
