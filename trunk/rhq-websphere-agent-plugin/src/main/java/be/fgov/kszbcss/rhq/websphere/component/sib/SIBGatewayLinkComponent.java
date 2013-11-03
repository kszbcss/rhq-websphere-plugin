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

import java.util.Set;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.proxy.SIBGatewayLink;
import be.fgov.kszbcss.rhq.websphere.support.measurement.JMXOperationMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.SimpleMeasurementHandler;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.sib.admin.SIBLinkReceiver;

public class SIBGatewayLinkComponent extends WebSphereServiceComponent<SIBMessagingEngineComponent> implements MeasurementFacet {
    private static final Log log = LogFactory.getLog(SIBGatewayLinkComponent.class);
    
    private SIBGatewayLink gatewayLink;
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        SIBMessagingEngineComponent me = getResourceContext().getParentResourceComponent();
        ApplicationServer server = getServer();
        String name = getResourceContext().getResourceKey();
        gatewayLink = server.getMBeanClient(new SIBGatewayLinkMBeanLocator(me, name)).getProxy(SIBGatewayLink.class);
        MBeanClient linkTransmitter = server.getMBeanClient(new SIBLinkTransmitterMBeanLocator(me, name));
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("depth", new JMXOperationMeasurementHandler(linkTransmitter, "getDepth", false));
        measurementFacetSupport.addHandler("numberOfMessagesSent", new JMXOperationMeasurementHandler(linkTransmitter, "getNumberOfMessagesSent", false));
        measurementFacetSupport.addHandler("numberOfMessagesReceived", new SimpleMeasurementHandler() {
            @Override
            protected Object getValue() throws JMException, ConnectorException {
                long numberOfMessagesReceived = 0;
                SIBLinkReceiver[] receivers = gatewayLink.listLinkReceivers();
                if (receivers != null) {
                    for (SIBLinkReceiver receiver : receivers) {
                        numberOfMessagesReceived += receiver.getNumberOfMessagesReceived();
                    }
                }
                return numberOfMessagesReceived;
            }
        });
    }

    @Override
    protected boolean isConfigured() throws Exception {
        SIBMessagingEngineInfo meInfo = getResourceContext().getParentResourceComponent().getInfo();
        return meInfo != null && meInfo.getTargetUUIDForGatewayLink(getResourceContext().getResourceKey()) != null;
    }

    @Override
    protected AvailabilityType doGetAvailability() {
        try {
            if (getResourceContext().getParentResourceComponent().isActive()) {
                return gatewayLink.isActive() ? AvailabilityType.UP : AvailabilityType.DOWN;
            } else {
                log.debug("Message engine not started ==> availability = UP");
                return AvailabilityType.UP;
            }
        } catch (Exception ex) {
            log.debug("Call to isActive failed ==> availability = DOWN", ex);
            return AvailabilityType.DOWN;
        }
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public void stop() {
    }
}
