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
package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.JMException;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.component.InDoubtTransactionsMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.proxy.SIBMessagingEngine;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;

public class TransactionsComponent extends WebSphereServiceComponent<SIBMessagingEngineComponent> implements MeasurementFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ApplicationServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(),
                "SIB Service", "SIB Messaging Engines", getResourceContext().getParentResourceComponent().getName(),
                "Storage Management", "Transactions"));
        final SIBMessagingEngine sibMessagingEngine = getResourceContext().getParentResourceComponent().getSibMessagingEngine();
        measurementFacetSupport.addHandler("IndoubtTransactions", new InDoubtTransactionsMeasurementHandler() {
            @Override
            protected Set<String> getTransactionIds() throws JMException, ConnectorException {
                Set<String> ids = new HashSet<String>();
                for (Iterator<?> it = sibMessagingEngine.getPreparedTransactions().iterator(); it.hasNext(); ) {
                    ids.add((String)it.next());
                }
                return ids;
            }
        });
    }

    @Override
    protected boolean isConfigured() throws Exception {
        // The SIB messaging engine cache is always configured
        return true;
    }

    protected AvailabilityType doGetAvailability() {
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        if (getResourceContext().getParentResourceComponent().isActive()) {
            measurementFacetSupport.getValues(report, requests);
        }
    }

    public void stop() {
    }
}
