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
package be.fgov.kszbcss.rhq.websphere.component.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.ObjectName;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.event.EventContext;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.UnmanagedServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereComponent;
import be.fgov.kszbcss.rhq.websphere.component.server.log.J2EEComponentKey;
import be.fgov.kszbcss.rhq.websphere.component.server.log.LoggingProvider;
import be.fgov.kszbcss.rhq.websphere.component.server.log.none.NoneLoggingProvider;
import be.fgov.kszbcss.rhq.websphere.component.server.log.ras.RasLoggingProvider;
import be.fgov.kszbcss.rhq.websphere.component.server.log.xm4was.XM4WASLoggingProvider;
import be.fgov.kszbcss.rhq.websphere.config.ConfigObjectNotFoundException;
import be.fgov.kszbcss.rhq.websphere.connector.ems.WebsphereConnectionProvider;
import be.fgov.kszbcss.rhq.websphere.proxy.EJBMonitor;
import be.fgov.kszbcss.rhq.websphere.proxy.J2CMessageEndpoint;
import be.fgov.kszbcss.rhq.websphere.proxy.WebSphereJVM;
import be.fgov.kszbcss.rhq.websphere.proxy.Server;
import be.fgov.kszbcss.rhq.websphere.proxy.TraceService;
import be.fgov.kszbcss.rhq.websphere.proxy.XM4WASJVM;
import be.fgov.kszbcss.rhq.websphere.support.measurement.JMXAttributeGroupHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class WebSphereServerComponent extends WebSphereComponent<ResourceComponent<?>> implements MeasurementFacet, ConfigurationFacet {
    private static final Log log = LogFactory.getLog(WebSphereServerComponent.class);
    
    private static final Map<String,Class<? extends LoggingProvider>> loggingProviderClasses;
    
    static {
        loggingProviderClasses = new HashMap<String,Class<? extends LoggingProvider>>();
        loggingProviderClasses.put("none", NoneLoggingProvider.class);
        loggingProviderClasses.put("ras", RasLoggingProvider.class);
        loggingProviderClasses.put("xm4was", XM4WASLoggingProvider.class);
    }
    
    private String cell;
    private String node;
    private String serverName;
    private ResourceContext<ResourceComponent<?>> resourceContext;
    private File stateDir;
    private ApplicationServer server;
    private EmsConnection connection;
    private MeasurementFacetSupport measurementFacetSupport;
    private String loggingProviderName;
    private LoggingProvider loggingProvider;
    private WebSphereJVM wasJvm;
    private XM4WASJVM xm4wasJvm;
    private EJBMonitor ejbMonitor;
    
    public void start(ResourceContext<ResourceComponent<?>> context) throws InvalidPluginConfigurationException, Exception {
        this.resourceContext = context;
        
        Configuration pluginConfiguration = context.getPluginConfiguration();
        
        String[] parts = context.getResourceKey().split("/");
        cell = parts[0];
        node = parts[1];
        serverName = parts[2];
        stateDir = new File(new File(new File(new File(getResourceContext().getDataDirectory(), "state"), cell), node), serverName);
        
        loggingProviderName = pluginConfiguration.getSimpleValue("loggingProvider", "none");
        Class<? extends LoggingProvider> loggingProviderClass = loggingProviderClasses.get(loggingProviderName);
        if (loggingProviderClass == null) {
            throw new InvalidPluginConfigurationException("Unknown logging provider '" + loggingProviderName + "'");
        }
        if (log.isDebugEnabled()) {
            log.debug("Creating logging provider: name=" + loggingProviderName + ", class=" + loggingProviderClass.getName());
        }
        loggingProvider = loggingProviderClass.newInstance();
        
        PropertySimple unmanaged = pluginConfiguration.getSimple("unmanaged");
        if (unmanaged != null && unmanaged.getBooleanValue()) {
            server = new UnmanagedServer(cell, node, serverName, pluginConfiguration);
        } else {
            server = new ManagedServer(cell, node, serverName, pluginConfiguration);
        }
        server.init();
        
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.setDefaultHandler(new JMXAttributeGroupHandler(server.getServerMBean()));
        
        log.debug("Starting logging provider");
        loggingProvider.start(server, context.getEventContext(), EventPublisherImpl.INSTANCE, loadLoggingState());
        
        wasJvm = server.getMBeanClient("WebSphere:type=JVM,*").getProxy(WebSphereJVM.class);
        xm4wasJvm = server.getMBeanClient("XM4WAS:type=JVM,*").getProxy(XM4WASJVM.class);
        
        // TODO: the EJBMonitor MBean is not necessarily registered; maybe we need something to prevent the plug-in from querying the server repeatedly for the same MBean
        ejbMonitor = server.getMBeanClient("XM4WAS:type=EJBMonitor,*").getProxy(EJBMonitor.class);
    }

    public ResourceContext<ResourceComponent<?>> getResourceContext() {
        return resourceContext;
    }

    public ApplicationServer getServer() {
        return server;
    }

    public synchronized EmsConnection getEmsConnection() {
        if (connection == null) {
            Configuration pluginConfig = resourceContext.getPluginConfiguration();
            ConnectionSettings connectionSettings = new ConnectionSettings();
            connectionSettings.setServerUrl(pluginConfig.getSimpleValue("host", null) + ":" + pluginConfig.getSimpleValue("port", null));
            ConnectionProvider connectionProvider = new WebsphereConnectionProvider(server.getAdminClient());
            // The connection settings are not required to establish the connection, but they
            // will still be used in logging:
            connectionProvider.initialize(connectionSettings);
            connection = connectionProvider.connect();
            
            // If this is not present, then EmbeddedJMXServerDiscoveryComponent will fail to
            // discover the platform MXBeans.
            connection.loadSynchronous(false);
        }
        return connection;
    }
    
    @Override
    protected boolean isConfigured() throws Exception {
        ApplicationServer server = getServer();
        try {
            // This effectively checks that the server still exists in the cell configuration.
            // Of course this will not always work:
            //  * If the server is unmanaged, then the cell configuration is managed by that
            //    server and we cannot really check if the server is still configured.
            //  * If the server is managed, then we rely on the deployment manager connection
            //    still being available after the server has been removed. This will not
            //    always be the case, in particular if the RHQ agent is restarted.
            server.queryConfig(new ClusterNameQuery(server.getNode(), server.getServer()));
            return true;
        } catch (ConfigObjectNotFoundException ex) {
            // We must return false only if the configuration object corresponding to the server
            // has not been found. That means that the type of exception specified in the catch
            // statement is really important.
            return false;
        }
    }

    @Override
    protected AvailabilityType doGetAvailability() {
        if (log.isDebugEnabled()) {
            log.debug("Starting to determine availability for server " + getResourceContext().getResourceKey());
        }
        AdminClient adminClient = server.getAdminClient();
        ObjectName serverMBean;
        try {
            serverMBean = adminClient.getServerMBean();
        } catch (ConnectorException ex) {
            log.debug("Unable to get server MBean => server DOWN", ex);
            return AvailabilityType.DOWN;
        }
        String state;
        try {
            state = (String)adminClient.getAttribute(serverMBean, "state");
        } catch (ConnectorException ex) {
            log.debug("Failed to get 'state' attribute from the server MBean => server DOWN", ex);
            return AvailabilityType.DOWN;
        } catch (JMException ex) {
            log.warn("Unexpected management exception while getting the 'state' attribute from the server MBean", ex);
            return AvailabilityType.DOWN;
        } catch (JMRuntimeException ex) {
            log.warn("Unexpected management runtime exception while getting the 'state' attribute from the server MBean", ex);
            return AvailabilityType.DOWN;
        }
        if (log.isDebugEnabled()) {
            log.debug("Server state = " + state);
        }
        if (state.equals("STARTED")) {
            return AvailabilityType.UP;
        } else {
            return AvailabilityType.DOWN;
        }
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    @Override
    protected OperationResult doInvokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("restart")) {
            Server server = getServer().getServerMBean().getProxy(Server.class);
            server.restart();
        } else if (name.equals("pauseAllMessageEndpoints")) {
            changeMessageEndpointState(true);
        } else if (name.equals("resumeAllMessageEndpoints")) {
            changeMessageEndpointState(false);
        } else if (name.equals("appendTraceString")) {
            TraceService traceService = getServer().getMBeanClient("WebSphere:type=TraceService,*").getProxy(TraceService.class);
            traceService.appendTraceString(parameters.getSimpleValue("traceString", null));
        } else if (name.equals("generateSystemDump")) {
            boolean performGC = Boolean.valueOf(parameters.getSimpleValue("performGC"));
            // Use WebSphere's MBean if possible (because XM4WAS may not be installed on the target server)
            if (performGC) {
                wasJvm.generateSystemDump();
            } else {
                xm4wasJvm.generateSystemDump(false);
            }
        }
        return null;
    }
    
    private OperationResult changeMessageEndpointState(boolean pause) throws Exception {
        AdminClient adminClient = getServer().getAdminClient();
        for (ObjectName endpointObjectName : adminClient.queryNames(new ObjectName("WebSphere:type=J2CMessageEndpoint,*"), null)) {
            J2CMessageEndpoint endpoint = getServer().getMBeanClient(endpointObjectName).getProxy(J2CMessageEndpoint.class);
            if (pause) {
                endpoint.pause();
            } else {
                endpoint.resume();
            }
        }
        return null;
    }

    public Configuration loadResourceConfiguration() throws Exception {
        Configuration config = new Configuration();
        String clusterName = server.getClusterName();
        config.put(new PropertySimple("clusterName", clusterName));
        config.put(new PropertySimple("clusterKey", clusterName == null ? null : server.getCell() + "/" + clusterName));
        return config;
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
        throw new UnsupportedOperationException();
    }

    public void registerLogEventContext(String applicationName, String moduleName, String componentName, EventContext context) {
        loggingProvider.registerEventContext(new J2EEComponentKey(applicationName, moduleName, componentName), context);
    }
    
    public void unregisterLogEventContext(String applicationName, String moduleName, String componentName) {
        loggingProvider.unregisterEventContext(new J2EEComponentKey(applicationName, moduleName, componentName));
    }
    
    public EJBMonitor getEjbMonitor() {
        return ejbMonitor;
    }

    public void stop() {
        persistLoggingState(loggingProvider.stop());
        server.destroy();
    }
    
    private void persistLoggingState(String state) {
        if (log.isDebugEnabled()) {
            log.debug("Persisting the state of the logging provider: " + state);
        }
        if (stateDir.exists() || stateDir.mkdirs()) {
            File stateFile = new File(stateDir, "logstate");
            if (state == null) {
                if (stateFile.exists()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Deleting " + stateFile.getAbsolutePath());
                    }
                    if (!stateFile.delete()) {
                        log.error("Failed to delete " + stateFile.getAbsolutePath());
                    }
                } else {
                    log.debug(stateFile.getAbsolutePath() + " doesn't exist; not taking any action");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Writing " + stateFile.getAbsolutePath());
                }
                try {
                    OutputStream out = new FileOutputStream(stateFile);
                    try {
                        OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
                        writer.write(loggingProviderName);
                        writer.write(':');
                        writer.write(state);
                        writer.flush();
                    } finally {
                        out.close();
                    }
                } catch (IOException ex) {
                    log.error("Failed to write " + stateFile, ex);
                }
            }
        } else {
            log.error("Failed to create directory " + stateDir.getAbsolutePath());
        }
    }
    
    private String loadLoggingState() {
        log.debug("Loading persistent state of the logging provider");
        File stateFile = new File(stateDir, "logstate");
        if (stateFile.exists()) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Reading " + stateDir.getAbsolutePath());
                }
                String content;
                try {
                    InputStream in = new FileInputStream(stateFile);
                    try {
                        content = IOUtils.toString(in, "UTF-8");
                    } finally {
                        in.close();
                    }
                } catch (IOException ex) {
                    log.error("Failed to read " + stateDir.getAbsolutePath(), ex);
                    return null;
                }
                int idx = content.indexOf(':');
                if (idx == -1) {
                    log.error("Unexpected content in file " + stateFile.getAbsolutePath());
                    return null;
                } else {
                    String previousProvider = content.substring(0, idx);
                    if (previousProvider.equals(loggingProviderName)) {
                        return content.substring(idx+1);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Previous logging provider (" + previousProvider + ") not the same as current provider (" + loggingProviderName + ")");
                        }
                        return null;
                    }
                }
            } finally {
                // We always delete the state file
                if (!stateFile.delete()) {
                    log.error("Unable to delete " + stateFile.getAbsolutePath());
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(stateDir.getAbsolutePath() + " doesn't exist; no persistent state available");
            }
            return null;
        }
    }
}
