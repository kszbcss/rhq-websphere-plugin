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
package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryInfo;
import be.fgov.kszbcss.rhq.websphere.component.JAASAuthData;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.jdbc.DataSourceComponent;
import be.fgov.kszbcss.rhq.websphere.proxy.AdminOperations;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;

public class DB2MonitorComponent extends WebSphereServiceComponent<DataSourceComponent> implements MeasurementFacet, ConfigurationFacet, OperationFacet {
    private static final Logger log = LoggerFactory.getLogger(DB2MonitorComponent.class);
    
    private DataSourceComponent dataSourceComponent;
    private AdminOperations adminOperations;
    private String principal;
    private String credentials;
    private DB2MonitorContext context;
    private MeasurementFacetSupport measurementFacetSupport;
    
    protected void doStart() throws InvalidPluginConfigurationException {
        dataSourceComponent = getParent();
        adminOperations = dataSourceComponent.getServer().getMBeanClient("WebSphere:type=AdminOperations,*").getProxy(AdminOperations.class);
        Configuration config = getResourceContext().getPluginConfiguration();
        principal = config.getSimpleValue("principal", null);
        credentials = config.getSimpleValue("credentials", null);
        try {
            Class.forName("com.ibm.db2.jcc.DB2SimpleDataSource");
        } catch (ClassNotFoundException ex) {
            log.error("DB2 monitor unavailable: JDBC driver not present in the class path");
            throw new Error(ex);
        }
        if (principal != null) {
            measurementFacetSupport = new MeasurementFacetSupport(this);
            measurementFacetSupport.setDefaultHandler(new SnapshotMeasurementGroupHandler(this, adminOperations));
            measurementFacetSupport.addHandler("acr", new ACRMeasurementGroupHandler(this));
        }
    }

    @Override
    protected AvailabilityType doGetAvailability() {
        try {
            getContext().testConnection();
            return AvailabilityType.UP;
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Test connection failed ==> availability = DOWN", ex);
            }
            return AvailabilityType.DOWN;
        }
    }

    @Override
    protected boolean isConfigured() throws Exception {
        return true;
    }

    public synchronized DB2MonitorContext getContext() throws Exception {
        ConnectionFactoryInfo connectionFactoryInfo = dataSourceComponent.getConnectionFactoryInfo();
        Map<String,Object> dataSourceProperties = connectionFactoryInfo.getProperties();
        if (context == null || !context.getDataSourceProperties().equals(dataSourceProperties)) {
            if (context != null) {
                context.destroy();
                // Set context to null so that we remain in a consistent state even if something goes
                // wrong before we create the new context.
                context = null;
            }
            String effectivePrincipal = null;
            String effectiveCredentials = null;
            if (principal != null) {
                log.debug("Using credentials specified in the plug-in configuration");
                effectivePrincipal = principal;
                effectiveCredentials = credentials;
            } else {
                String authDataAlias = connectionFactoryInfo.getAuthDataAlias();
                if (authDataAlias == null) {
                    log.debug("No authentication alias found");
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Loading data for authentication alias " + authDataAlias);
                    }
                    JAASAuthData authData = getParent().getParent().getJAASAuthDataMap().getData(authDataAlias);
                    if (authData == null) {
                        log.warn("No data found for authentication alias " + authDataAlias);
                    } else {
                        effectivePrincipal = authData.getUserId();
                        effectiveCredentials = authData.getPassword();
                    }
                }
            }
            context = new DB2MonitorContext(dataSourceProperties, effectivePrincipal, effectiveCredentials);
        }
        return context;
    }
    
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        if (measurementFacetSupport == null) {
            log.warn("No monitoring user defined for data source "
                    + getParent().getResourceContext().getResourceKey()
                    + "; unable to collect measurements");
        } else {
            measurementFacetSupport.getValues(report, requests);
        }
    }

    public Configuration loadResourceConfiguration() throws Exception {
        Configuration config = new Configuration();
        Map<String,Object> dsProps = getContext().getDataSourceProperties();
        config.put(new PropertySimple("primary", dsProps.get("serverName") + ":" + dsProps.get("portNumber")));
        String alternateServerName = (String)dsProps.get("clientRerouteAlternateServerName");
        if (alternateServerName != null && alternateServerName.length() > 0) {
            config.put(new PropertySimple("alternate", alternateServerName + ":" + dsProps.get("clientRerouteAlternatePortNumber")));
        } else {
            config.put(new PropertySimple("alternate", null));
        }
        config.put(new PropertySimple("databaseName", (String)dsProps.get("databaseName")));
        if (log.isDebugEnabled()) {
            log.debug("Loaded resource configuration: " + config.toString(true));
        }
        return config;
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
        // Empty: all properties are read only
    }

    @Override
    public OperationResult invokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("testConnection")) {
            getContext().testConnection();
            return null;
        } else {
           	return null;
        }
    }

    @Override
    protected void doStop() {
        if (context != null) {
            context.destroy();
            context = null;
        }
    }
}
