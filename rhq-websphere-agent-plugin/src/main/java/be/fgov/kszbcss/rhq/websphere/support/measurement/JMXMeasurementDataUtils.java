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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

public class JMXMeasurementDataUtils {
    private static final Logger log = LoggerFactory.getLogger(JMXMeasurementDataUtils.class);
    
    private JMXMeasurementDataUtils() {}
    
    public static void addData(MeasurementReport report, MeasurementScheduleRequest request, Object value) {
        switch (request.getDataType()) {
            case MEASUREMENT:
                Double doubleValue;
                if (value instanceof Double) {
                    doubleValue = (Double)value;
                } else if (value instanceof Number) {
                    doubleValue = Double.valueOf(((Number)value).doubleValue());
                } else {
                    log.error("Type " + value.getClass() + " not support for numeric measurements");
                    break;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Adding (numeric) measurement for " + request.getName() + "; value=" + value);
                }
                report.addData(new MeasurementDataNumeric(request, doubleValue));
                break;
            case TRAIT:
                if (log.isDebugEnabled()) {
                    log.debug("Adding measurement (trait) for " + request.getName() + "; value=" + value);
                }
                report.addData(new MeasurementDataTrait(request, value == null ? null : value.toString()));
                break;
            default:
                log.error("Data type " + request.getDataType() + " not supported");
        }
    }
}
