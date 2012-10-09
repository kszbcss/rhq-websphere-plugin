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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.component.InDoubtTransactionsMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.proxy.TransactionService;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiConstants;

public class TransactionServiceComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(getServer().getServerMBean(), PmiConstants.TRAN_MODULE));
        final TransactionService transactionService = getServer().getMBeanClient("WebSphere:type=TransactionService,*").getProxy(TransactionService.class);
        measurementFacetSupport.addHandler("IndoubtTransactions", new InDoubtTransactionsMeasurementHandler() {
            @Override
            protected Set<String> getTransactionIds() throws JMException, ConnectorException {
                Set<String> ids = new HashSet<String>();
                ids.addAll(Arrays.asList(transactionService.listImportedPreparedTransactions()));
                ids.addAll(Arrays.asList(transactionService.listManualTransactions()));
                ids.addAll(Arrays.asList(transactionService.listRetryTransactions()));
                // In most cases, transactions with heuristic outcome are not problematic
                // (they usually indicate that WebSphere couldn't report the correct status back
                // to the application, but that the transaction status was resolved later). 
//                ids.addAll(Arrays.asList(transactionService.listHeuristicTransactions()));
                return ids;
            }
        });
    }

    @Override
    protected boolean isConfigured() throws Exception {
        return true;
    }

    @Override
    protected AvailabilityType doGetAvailability() {
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public void stop() {
    }
}
