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
package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.Arrays;
import java.util.Set;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;

public abstract class SIBLocalizationPointComponent extends WebSphereServiceComponent<SIBMessagingEngineComponent> implements MeasurementFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void doStart() throws InvalidPluginConfigurationException {
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(
                new SIBMessagingEnginePMIModuleSelector(getParent(), "Destinations", getPMIModuleName(), getResourceContext().getResourceKey())));
    }
    
    protected abstract SIBDestinationType getType();
    protected abstract String getPMIModuleName();

    @Override
    protected boolean isConfigured() throws Exception {
        String[] destinationNames = getParent().getInfo().getDestinationNames(getType());
        return destinationNames!= null && Arrays.asList(destinationNames).contains(getResourceContext().getResourceKey());
    }
    
    protected AvailabilityType doGetAvailability() {
        // TODO: if the messaging engine is active, we should check that the corresponding MBean is registered and that the state reported by the MBean is ACTIVE
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        if (getParent().isActive()) {
            measurementFacetSupport.getValues(report, requests);
        }
    }
}
