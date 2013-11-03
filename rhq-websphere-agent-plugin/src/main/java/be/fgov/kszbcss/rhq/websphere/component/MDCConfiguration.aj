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
package be.fgov.kszbcss.rhq.websphere.component;

import org.apache.log4j.MDC;
import org.rhq.core.pluginapi.availability.AvailabilityFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;

public aspect MDCConfiguration {
    private static void collectKey(ResourceContext<?> context, StringBuilder buffer) {
        ResourceComponent<?> parentComponent = context.getParentResourceComponent();
        if (parentComponent instanceof WebSphereComponent<?>) {
            collectKey(((WebSphereComponent<?>)parentComponent).getResourceContext(), buffer);
        }
        if (buffer.length() > 0) {
            buffer.append('>');
        }
        buffer.append(context.getResourceKey());
    }
    
    Object around(WebSphereComponent instance): within(WebSphereComponent+) && execution(* (ConfigurationFacet || OperationFacet || AvailabilityFacet || MeasurementFacet).*(..)) && target(instance) {
        ResourceContext<?> context = instance.getResourceContext();
        StringBuilder key = new StringBuilder();
        collectKey(context, key);
        MDC.put("resourceType", context.getResourceType().getName());
        MDC.put("resourceKey", key.toString());
        try {
            return proceed(instance);
        } finally {
            MDC.remove("resourceType");
            MDC.remove("resourceKey");
        }
    }
}
