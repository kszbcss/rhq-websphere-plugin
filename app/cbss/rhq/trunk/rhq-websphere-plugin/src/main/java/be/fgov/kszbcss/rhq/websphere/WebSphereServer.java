package be.fgov.kszbcss.rhq.websphere;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.connector.AdminClientProvider;
import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStatsCollector;
import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStatsWrapper;
import be.fgov.kszbcss.rhq.websphere.connector.FailFastAdminClientProvider;
import be.fgov.kszbcss.rhq.websphere.connector.LazyAdminClientInvocationHandler;
import be.fgov.kszbcss.rhq.websphere.connector.SecureAdminClient;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClientFactory;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanLocator;
import be.fgov.kszbcss.rhq.websphere.mbean.ProcessInfo;
import be.fgov.kszbcss.rhq.websphere.proxy.Perf;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiModuleConfig;
import com.ibm.websphere.pmi.stat.MBeanLevelSpec;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.WSStats;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;

/**
 * Represents a WebSphere process (application server, node agent or deployment manager) and
 * provides access to administrative actions on that process. The class manages a JMX connection to
 * the process. Note that this connection is created lazily, so that an instance of this class can
 * be created successfully even if the server is unavailable.
 */
public abstract class WebSphereServer {
    private static class NotificationListenerRegistration {
        private final ObjectName name;
        private final NotificationListener listener;
        private final NotificationFilter filter;
        private final Object handback;
        private final boolean extended;
        private boolean isRegistered;
        
        NotificationListenerRegistration(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback, boolean extended) {
            this.name = name;
            this.listener = listener;
            this.filter = filter;
            this.handback = handback;
            this.extended = extended;
        }
        
        synchronized void doRegister(AdminClient adminClient) throws InstanceNotFoundException, ConnectorException {
            if (!isRegistered) {
                if (extended) {
                    adminClient.addNotificationListenerExtended(name, listener, filter, handback);
                } else {
                    adminClient.addNotificationListener(name, listener, filter, handback);
                }
                isRegistered = true;
            }
        }
        
        synchronized void doUnregister(AdminClient adminClient) throws InstanceNotFoundException, ListenerNotFoundException, ConnectorException {
            if (isRegistered) {
                if (extended) {
                    adminClient.removeNotificationListenerExtended(name, listener);
                } else {
                    adminClient.removeNotificationListener(name, listener);
                }
                isRegistered = false;
            }
        }
    }
    
    private static final Log log = LogFactory.getLog(WebSphereServer.class);
    
    private final ProcessLocator processLocator;
    private final MBeanClientFactory mbeanClientFactory;
    private final List<NotificationListenerRegistration> listeners = new ArrayList<NotificationListenerRegistration>();
    private final MBeanClient serverMBean;
    private AdminClient adminClient;
    private final Perf perf;
    private PmiModuleConfig[] pmiModuleConfigs;
    
    public WebSphereServer(ProcessLocator processLocator) {
        this.processLocator = processLocator;
        mbeanClientFactory = new MBeanClientFactory(this);
        serverMBean = getMBeanClient(new MBeanLocator() {
            public Set<ObjectName> queryNames(ProcessInfo processInfo, AdminClient adminClient) throws JMException, ConnectorException {
                return Collections.singleton(adminClient.getServerMBean());
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
        // TODO: synchronization???
        for (NotificationListenerRegistration registration : listeners) {
            try {
                // TODO: skip the call to getAdminClient if the listener is actually not registered
                registration.doUnregister(getAdminClient());
            } catch (Exception ex) {
                log.error("Failed to unregister listener", ex);
            }
        }
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

    public synchronized AdminClient getAdminClient() throws ConnectorException {
        if (adminClient == null) {
            final Properties properties = new Properties();
            
            try {
                processLocator.getAdminClientProperties(properties);
            } catch (JMException ex) {
                throw new ConnectorException(ex);
            }
            
            // From the IBM site: "When you use the createAdminClient method within application code that
            // runs on an application server, such as within servlets and JavaServer Pages (JSP) files,
            // you must set the CACHE_DISABLED property to true." Since we use multiple threads and access
            // multiple servers, we assume that this also applies to us.
            properties.setProperty(AdminClient.CACHE_DISABLED, "true");
            
            if (log.isDebugEnabled()) {
                log.debug("Creating AdminClient with properties: " + properties);
            }
            
            AdminClientProvider provider = new FailFastAdminClientProvider(new AdminClientProvider() {
                public AdminClient createAdminClient() throws ConnectorException {
                    AdminClient adminClient = AdminClientFactory.createAdminClient(properties);
                    
                    adminClient = new AdminClientStatsWrapper(adminClient, AdminClientStatsCollector.INSTANCE);
                    
                    try {
                        Subject subject = WSSubject.getRunAsSubject();
                        if (log.isDebugEnabled()) {
                            log.debug("Subject = " + subject);
                        }
                        if (subject != null) {
                            WSSubject.setRunAsSubject(null);
                            adminClient = new SecureAdminClient(adminClient, subject);
                        }
                    } catch (WSSecurityException ex) {
                        throw new ConnectorException(ex);
                    }
                    
                    return adminClient;
                }
            });
            
            adminClient = (AdminClient)Proxy.newProxyInstance(WebSphereServer.class.getClassLoader(),
                    new Class<?>[] { AdminClient.class },
                    new LazyAdminClientInvocationHandler(provider));
            
            for (NotificationListenerRegistration registration : listeners) {
                try {
                    registration.doRegister(adminClient);
                } catch (Exception ex) {
                    log.error("(Deferred) listener registration failed", ex); 
                }
            }
            
            // TODO: we should check here that we are connecting to the right server
        }
        return adminClient;
    }
    
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback, boolean extended) {
        NotificationListenerRegistration registration = new NotificationListenerRegistration(name, listener, filter, handback, extended);
        try {
            registration.doRegister(getAdminClient());
        } catch (Exception ex) {
            log.info("Listener registration failed; will try later");
        }
        // TODO: probably we need synchronization here
        listeners.add(registration);
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
