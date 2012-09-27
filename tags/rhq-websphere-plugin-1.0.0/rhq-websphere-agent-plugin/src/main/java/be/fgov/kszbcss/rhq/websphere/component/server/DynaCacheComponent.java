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
package be.fgov.kszbcss.rhq.websphere.component.server;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.proxy.DynaCache;

public class DynaCacheComponent implements ResourceComponent<WebSphereServerComponent>, MeasurementFacet, OperationFacet {
    private static final Log log = LogFactory.getLog(DynaCacheComponent.class);
    
    private DynaCache cache;
    private String instanceName;

    public void start(ResourceContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        cache = context.getParentResourceComponent().getServer().getMBeanClient("WebSphere:type=DynaCache,*").getProxy(DynaCache.class);
        instanceName = context.getResourceKey();
    }

    public AvailabilityType getAvailability() {
        String[] instanceNames;
        try {
            instanceNames = cache.getCacheInstanceNames();
        } catch (Exception ex) {
            log.error("Unable to get cache instance names", ex);
            return AvailabilityType.DOWN;
        }
        for (String name : instanceNames) {
            if (name.equals(instanceName)) {
                return AvailabilityType.UP;
            }
        }
        return AvailabilityType.DOWN;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        String[] stats = cache.getAllCacheStatistics(instanceName);
        for (MeasurementScheduleRequest request : requests) {
            String name = request.getName();
            String value = null;
            for (String stat : stats) {
                if (stat.indexOf('=') == name.length() && stat.startsWith(name)) {
                    value = stat.substring(name.length()+1);
                    break;
                }
            }
            if (value != null) {
                report.addData(new MeasurementDataNumeric(request, Double.parseDouble(value)));
            }
        }
    }

    public OperationResult invokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("clearCache")) {
            cache.clearCache(instanceName);
        }
        return null;
    }

    public void stop() {
    }
}
