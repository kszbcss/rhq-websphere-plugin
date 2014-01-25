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
package be.fgov.kszbcss.rhq.websphere.process;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.management.JMException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStatsCollector;
import be.fgov.kszbcss.rhq.websphere.connector.FailFastAdminClientProvider;
import be.fgov.kszbcss.rhq.websphere.connector.LazyAdminClientInvocationHandler;
import be.fgov.kszbcss.rhq.websphere.connector.SecureAdminClientProvider;
import be.fgov.kszbcss.rhq.websphere.connector.StatsCollectingAdminClientProvider;
import be.fgov.kszbcss.rhq.websphere.connector.notification.NotificationListenerManager;
import be.fgov.kszbcss.rhq.websphere.connector.notification.NotificationListenerRegistration;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClientFactory;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanLocator;
import be.fgov.kszbcss.rhq.websphere.process.locator.ProcessLocator;
import be.fgov.kszbcss.rhq.websphere.proxy.Perf;
import be.fgov.kszbcss.rhq.websphere.util.PIDChangeTracker;
import be.fgov.kszbcss.rhq.websphere.util.PIDWatcher;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiModuleConfig;
import com.ibm.websphere.pmi.stat.MBeanLevelSpec;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.WSStats;

/**
 * Represents a WebSphere process (application server, node agent or deployment manager) and
 * provides access to administrative actions on that process. The class manages a JMX connection to
 * the process. Note that this connection is created lazily, so that an instance of this class can
 * be created successfully even if the server is unavailable.
 */
public abstract class WebSphereServer {
    private static final Log log = LogFactory.getLog(WebSphereServer.class);
    
    private final ProcessLocator processLocator;
    private final ProcessIdentityValidator processIdentityValidator;
    private final MBeanClientFactory mbeanClientFactory;
    private final MBeanClient serverMBean;
    private final AdminClient adminClient;
    private final PIDWatcher pidWatcher;
    private final PIDChangeTracker pmiConfigRefreshTracker;
    private final NotificationListenerManager notificationListenerManager;
    private final Perf perf;
    private PmiModuleConfig[] pmiModuleConfigs;
    
    public WebSphereServer(String cell, String node, String server, String processType, ProcessLocator processLocator) {
        this.processLocator = processLocator;
        
        // Notes:
        //  * The stats collection wrapper is applied before security because the security wrapper may rearrange
        //    some invocations, but we want the statistics to be as accurate as possible.
        //  * Process identity validation is handled after security because the ProcessIdentityValidator may
        //    prematurely create the AdminClient on a different thread.
        //  * The fail-fast feature is added last.
        processIdentityValidator = new ProcessIdentityValidator(new SecureAdminClientProvider(
                new StatsCollectingAdminClientProvider(processLocator, AdminClientStatsCollector.INSTANCE)), cell, node, server, processType);
        adminClient = (AdminClient)Proxy.newProxyInstance(WebSphereServer.class.getClassLoader(),
                new Class<?>[] { AdminClient.class },
                new LazyAdminClientInvocationHandler(new FailFastAdminClientProvider(processIdentityValidator)));
        
        pidWatcher = new PIDWatcher(adminClient);
        pmiConfigRefreshTracker = pidWatcher.createTracker();
        
        notificationListenerManager = new NotificationListenerManager(adminClient, pidWatcher);
        mbeanClientFactory = new MBeanClientFactory(this);
        serverMBean = getMBeanClient(new MBeanLocator() {
            public Set<ObjectName> queryNames(WebSphereServer server) throws JMException, ConnectorException {
                return Collections.singleton(server.getAdminClient().getServerMBean());
            }
        });
        perf = getMBeanClient("WebSphere:type=Perf,*").getProxy(Perf.class);
    }
    
    public ProcessLocator getProcessLocator() {
        return processLocator;
    }

    public void init() {
    }
    
    public void destroy() {
    }
    
    /**
     * Get the cell name. A subclass may override this method if it is able to return the cell name
     * without the possibility of getting a {@link ConnectorException}.
     * 
     * @return the cell name
     * @throws ConnectorException
     */
    public String getCell() throws ConnectorException {
        return processIdentityValidator.getCell();
    }

    /**
     * Get the node name. A subclass may override this method if it is able to return the node name
     * without the possibility of getting a {@link ConnectorException}.
     * 
     * @return the node name
     * @throws ConnectorException
     */
    public String getNode() throws ConnectorException {
        return processIdentityValidator.getNode();
    }

    /**
     * Get the server name. A subclass may override this method if it is able to return the server
     * name without the possibility of getting a {@link ConnectorException}.
     * 
     * @return the server name
     * @throws ConnectorException
     */
    public String getServer() throws ConnectorException {
        return processIdentityValidator.getServer();
    }

    public String getProcessType() throws ConnectorException {
        return processIdentityValidator.getProcessType();
    }
    
    public MBeanClient getMBeanClient(MBeanLocator locator) {
        return mbeanClientFactory.getMBeanClient(locator);
    }

    public MBeanClient getMBeanClient(ObjectName objectNamePattern) {
        return mbeanClientFactory.getMBeanClient(objectNamePattern);
    }

    public MBeanClient getMBeanClient(String objectNamePattern) {
        return mbeanClientFactory.getMBeanClient(objectNamePattern);
    }

    public MBeanClient getServerMBean() {
        return serverMBean;
    }

    public AdminClient getAdminClient() {
        return adminClient;
    }
    
    public NotificationListenerRegistration addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback, boolean extended) {
        return notificationListenerManager.addNotificationListener(name, listener, filter, handback, extended);
    }
    
    public WSStats getWSStats(MBeanStatDescriptor descriptor) throws JMException, ConnectorException {
        WSStats stats = perf.getStatsObject(descriptor, Boolean.TRUE);
        if (log.isDebugEnabled()) {
            if (stats == null) {
                log.debug("No stats found for " + descriptor);
            } else {
                log.debug("Loaded statistics for " + descriptor
                        + ":\n  WSStats class: " + stats.getClass().getName()
                        + "\n  Stats type: " + stats.getStatsType()
                        + "\n  Available statistics: " + Arrays.asList(stats.getStatisticNames()));
            }
        }
        return stats;
    }
    
    public synchronized PmiModuleConfig getPmiModuleConfig(WSStats stats) throws JMException, ConnectorException {
        String statsType = stats.getStatsType();
        int dashIndex = statsType.indexOf('#');
        if (dashIndex != -1) {
            statsType = statsType.substring(0, dashIndex);
        }
        
        // Reload PMI configs after WebSphere restart (the WebSphere installation may have been
        // updated and new PMI metrics may be available)
        if (pmiConfigRefreshTracker.isRestarted()) {
            log.debug("Discarding cached PMI config after server restart");
            pmiModuleConfigs = null;
        }
        
        // Implementation note: PMI module configurations are not necessarily registered
        // immediately at server startup. Therefore, if we don't find the module configuration
        // in the cached data, we need to reload the data and try again. This problem has been
        // observed with the SIBus PMI modules.
        boolean pmiModuleConfigsReloaded = false;
        while (true) {
            if (pmiModuleConfigs == null) {
                pmiModuleConfigs = perf.getConfigs();
                pmiModuleConfigsReloaded = true;
            }
            for (PmiModuleConfig config : pmiModuleConfigs) {
                if (config.getUID().equals(statsType)) {
                    return config;
                }
            }
            if (pmiModuleConfigsReloaded) {
                log.error("Unable to locate PMI module config for " + statsType);
                return null;
            } else {
                pmiModuleConfigs = null;
            }
        }
    }
    
    /**
     * 
     * 
     * @param descriptor
     * @param statisticsToEnable
     * @return the set of statistics that have effectively been enabled; it excludes statistics that
     *         were already enabled before the call
     */
    public Set<Integer> enableStatistics(MBeanStatDescriptor descriptor, Set<Integer> statisticsToEnable) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to enable statistics " + statisticsToEnable + " on " + descriptor);
            }
            
            // Load the existing instrumentation level
            MBeanLevelSpec[] specs = perf.getInstrumentationLevel(descriptor, Boolean.FALSE);
            if (specs.length != 1) {
                log.error("Expected getInstrumentationLevel to return exactly one MBeanLevelSpec object");
                return Collections.emptySet();
            }
            MBeanLevelSpec spec = specs[0];
            if (log.isDebugEnabled()) {
                log.debug("Current instrumentation level: " + spec);
            }
            
            // Add the IDs of the statistics to be enabled
            Set<Integer> enabledStatistics = new LinkedHashSet<Integer>();
            for (int id : spec.getEnabled()) {
                enabledStatistics.add(id);
            }
            Set<Integer> alreadyEnabled = new TreeSet<Integer>();
            Set<Integer> newStats = new TreeSet<Integer>();
            for (Integer dataId : statisticsToEnable) {
                if (enabledStatistics.add(dataId)) {
                    newStats.add(dataId);
                } else {
                    alreadyEnabled.add(dataId);
                }
            }
            
            if (!alreadyEnabled.isEmpty()) {
                log.info("The statistics " + alreadyEnabled + " appear to be already enabled on " + descriptor);
            }
            
            if (!newStats.isEmpty()) {
                int[] enabled = new int[enabledStatistics.size()];
                int index = 0;
                for (Integer dataId : enabledStatistics) {
                    enabled[index++] = dataId;
                }
                spec.setEnabled(enabled);
                if (log.isDebugEnabled()) {
                    log.debug("New instrumentation level: " + spec);
                }
                
                // Now update the instrumentation level
                perf.setInstrumentationLevel(spec, Boolean.FALSE);
                
                log.info("Enabled statistics " + newStats + " on " + descriptor);
            }
            
            return newStats;
        } catch (Exception ex) {
            log.error(ex); // TODO
            return Collections.emptySet();
        }
    }
}
