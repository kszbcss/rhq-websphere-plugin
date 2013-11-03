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

import org.rhq.core.pluginapi.availability.AvailabilityFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;

/**
 * Aspect that determines the cache strategy to be used by the configuration query cache, based on
 * the entry point invoked by the plug-in container. E.g. if the plug-in container invokes
 * {@link AvailabilityFacet#getAvailability()}, then it is important that the invocation completes
 * as quickly as possible, but it is OK to use potentially stale data. On the other hand, if the
 * plug-in container invokes a method defined by {@link ResourceDiscoveryComponent}, then the code
 * should not use stale data, but it is OK if the call takes more time.
 * <p>
 * The advises store the selected strategy in a thread local defined by {@link CacheRefreshStrategy},
 * from where it is retrieved by the configuration cache. Note that this design ensures that the
 * code can be imported into an IDE that has no AspectJ support.
 */
public aspect CacheRefreshStrategyDecision {
    /**
     * Pointcut selecting methods that require that a stale entry in the query cache should be
     * refreshed immediately (in which case the method waits for the completion of the refresh).
     */
    pointcut requiresImmediateRefresh():
        within(ResourceComponent+) && execution(* (ConfigurationFacet || OperationFacet).*(..))
            || execution(* ResourceDiscoveryComponent.*(..));
    
    /**
     * Pointcut selecting methods that should complete quickly because their invocation may time
     * out. For these methods, the query cache may return stale entries and schedule them for
     * refresh later.
     */
    pointcut allowsDeferredRefresh():
        within(ResourceComponent+) && execution(* (AvailabilityFacet || MeasurementFacet).*(..));
    
    before(): requiresImmediateRefresh() {
        CacheRefreshStrategy.setImmediateRefresh(Boolean.TRUE);
    }
    
    before(): allowsDeferredRefresh() {
        CacheRefreshStrategy.setImmediateRefresh(Boolean.FALSE);
    }
    
    after(): requiresImmediateRefresh() || allowsDeferredRefresh() {
        CacheRefreshStrategy.setImmediateRefresh(null);
    }
}