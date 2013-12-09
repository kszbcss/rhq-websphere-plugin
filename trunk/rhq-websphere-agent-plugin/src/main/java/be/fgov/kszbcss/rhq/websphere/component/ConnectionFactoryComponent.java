/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2013 Crossroads Bank for Social Security
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

import java.util.Set;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.stat.WSRangeStatistic;

import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.process.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIModuleSelector;

public abstract class ConnectionFactoryComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet {
    private static final Log log = LogFactory.getLog(ConnectionFactoryComponent.class);
    
    protected String jndiName;
    protected MBeanClient mbean;
    private MeasurementFacetSupport measurementFacetSupport;

    protected abstract ConnectionFactoryType getType();

    @Override
    protected void start() throws InvalidPluginConfigurationException {
        jndiName = getResourceContext().getResourceKey();
        ApplicationServer server = getServer();
        mbean = server.getMBeanClient(new ConnectionFactoryMBeanLocator(getType(), jndiName));
        measurementFacetSupport = new MeasurementFacetSupport(this);
        PMIModuleSelector moduleSelector = new PMIModuleSelector() {
            public String[] getPath() throws JMException, ConnectorException, InterruptedException {
                ConnectionFactoryInfo cf;
                try {
                    cf = getConnectionFactoryInfo();
                } catch (ConfigQueryException ex) {
                    // TODO
                    throw new RuntimeException(ex);
                }
                return new String[] { getType().getPmiModule(), cf.getProviderName(), cf.getJndiName() };
            }
        };
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(), moduleSelector) {
            @Override
            protected double getValue(String name, WSRangeStatistic statistic) {
                if (name.equals("PercentMaxed") || name.equals("PercentUsed")) {
                    return ((double)statistic.getCurrent())/100;
                } else {
                    return super.getValue(name, statistic);
                }
            }
        });
    }
    
    public void stop() {
    }

    public ConnectionFactoryInfo getConnectionFactoryInfo() throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        ApplicationServer server = getServer();
        return server.queryConfig(new ConnectionFactoryQuery(server.getNode(), server.getServer(), getType())).getByJndiName(jndiName);
    }
    
    @Override
    protected boolean isConfigured() throws Exception {
        return getConnectionFactoryInfo() != null;
    }

    protected AvailabilityType doGetAvailability() {
        if (log.isDebugEnabled()) {
            log.debug("Starting to determine availability of " + jndiName);
        }
        try {
            mbean.getObjectName(true);
            log.debug("MBean found => availability == UP");
            return AvailabilityType.UP;
        } catch (Exception ex) {
            log.debug("MBean not found => availability == DOWN", ex);
            return AvailabilityType.DOWN;
        }
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }
}
