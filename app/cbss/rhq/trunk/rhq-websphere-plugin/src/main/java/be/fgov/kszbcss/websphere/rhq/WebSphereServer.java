package be.fgov.kszbcss.websphere.rhq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.security.auth.Subject;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.event.EventContext;

import be.fgov.kszbcss.websphere.rhq.connector.AdminClientStatsCollector;
import be.fgov.kszbcss.websphere.rhq.connector.AdminClientStatsWrapper;
import be.fgov.kszbcss.websphere.rhq.connector.SecureAdminClient;
import be.fgov.kszbcss.websphere.rhq.mbean.MBeanClient;
import be.fgov.kszbcss.websphere.rhq.mbean.MBeanClientFactory;
import be.fgov.kszbcss.websphere.rhq.mbean.MBeanLocator;
import be.fgov.kszbcss.websphere.rhq.repository.ConfigDocument;
import be.fgov.kszbcss.websphere.rhq.repository.ConfigDocumentFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiModuleConfig;
import com.ibm.websphere.pmi.stat.MBeanLevelSpec;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.WSStats;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;

public class WebSphereServer {
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
    
    private final String cell;
    private final String node;
    private final String server;
    private final Configuration config;
    private final MBeanClientFactory mbeanClientFactory;
    private final List<NotificationListenerRegistration> listeners = new ArrayList<NotificationListenerRegistration>();
    private final StateChangeEventDispatcher stateEventDispatcher = new StateChangeEventDispatcher();
    private final MBeanClient serverMBean;
    private AdminClient adminClient;
    private ObjectName perfMBean;
    private PmiModuleConfig[] pmiModuleConfigs;
    private CacheManager cacheManager;
    private Ehcache configDocumentCache;
    
    public WebSphereServer(String cell, String node, String server, Configuration config) {
        this.config = config;
        // TODO: we should check somewhere that we are connecting to the right server
        this.cell = cell;
        this.node = node;
        this.server = server;
        mbeanClientFactory = new MBeanClientFactory(this);
        serverMBean = getMBeanClient("WebSphere:type=Server,*");
    }
    
    public String getCell() {
        return cell;
    }

    public String getNode() {
        return node;
    }

    public String getServer() {
        return server;
    }

    public void init() {
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.setUpdateCheck(false);
        CacheConfiguration cacheConfig = new CacheConfiguration("ConfigRepository", 100);
        config.addCache(cacheConfig);
        cacheManager = CacheManager.create(config);
        configDocumentCache = new UpdatingSelfPopulatingCache(cacheManager.getCache("ConfigRepository"),
                new ConfigDocumentFactory(getMBeanClient("WebSphere:type=ConfigRepository,*")));
        
        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType("j2ee.state.starting");
        filter.enableType("j2ee.state.running");
        filter.enableType("j2ee.state.stopping");
        filter.enableType("j2ee.state.stopped");
        filter.enableType("j2ee.state.failed");
        try {
            addNotificationListener(new ObjectName("WebSphere:*"), stateEventDispatcher, filter, null, true);
        } catch (MalformedObjectNameException ex) {
            log.error(ex);
        }
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
        
        cacheManager.shutdown();
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
            Properties properties = new Properties();
            
            properties.put(AdminClient.CONNECTOR_TYPE, AdminClient.CONNECTOR_TYPE_RMI);
            properties.setProperty(AdminClient.CONNECTOR_HOST, config.getSimpleValue("host", null));
            properties.setProperty(AdminClient.CONNECTOR_PORT, config.getSimpleValue("port", null));

            String principal = config.getSimpleValue("principal", null); 
            if (principal != null && principal.length() > 0) { 
                properties.setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED, "true"); 
                properties.setProperty(AdminClient.USERNAME, principal); 
                properties.setProperty(AdminClient.PASSWORD, config.getSimpleValue("credentials", null)); 
            } else { 
                properties.setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED, "false");
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Creating AdminClient with properties: " + properties);
            }
            
            adminClient = AdminClientFactory.createAdminClient(properties);
            
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
            
            for (NotificationListenerRegistration registration : listeners) {
                try {
                    registration.doRegister(adminClient);
                } catch (Exception ex) {
                    log.error("(Deferred) listener registration failed", ex); 
                }
            }
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
    
    public void registerStateChangeEventContext(ObjectName objectNamePattern, EventContext context) {
        stateEventDispatcher.registerEventContext(objectNamePattern, context);
    }

    public void unregisterStateChangeEventContext(ObjectName objectNamePattern) {
        stateEventDispatcher.unregisterEventContext(objectNamePattern);
    }

    private synchronized ObjectName getPerfMBean() throws JMException, ConnectorException {
        if (perfMBean == null) {
            Set<ObjectName> names = getAdminClient().queryNames(new ObjectName("WebSphere:type=Perf,*"), null);
            // TODO: check that there is exactly one result
            perfMBean = names.iterator().next();
        }
        return perfMBean;
    }
    
    public WSStats getWSStats(MBeanStatDescriptor descriptor) throws JMException, ConnectorException {
        WSStats stats = (WSStats)getAdminClient().invoke(getPerfMBean(), "getStatsObject",
                new Object[] { descriptor, Boolean.TRUE },
                new String[] { MBeanStatDescriptor.class.getName(), Boolean.class.getName() });
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
    
    private synchronized PmiModuleConfig[] getPmiModuleConfigs() throws JMException, ConnectorException {
        if (pmiModuleConfigs == null) {
            pmiModuleConfigs = (PmiModuleConfig[])getAdminClient().invoke(getPerfMBean(), "getConfigs", new Object[0], new String[0]);
        }
        return pmiModuleConfigs;
    }
    
    public PmiModuleConfig getPmiModuleConfig(WSStats stats) throws JMException, ConnectorException {
        String statsType = stats.getStatsType();
        int dashIndex = statsType.indexOf('#');
        if (dashIndex != -1) {
            statsType = statsType.substring(0, dashIndex);
        }
        for (PmiModuleConfig config : getPmiModuleConfigs()) {
            if (config.getUID().equals(statsType)) {
                return config;
            }
        }
        log.error("Unable to locate PMI module config for " + statsType);
        return null;
    }
    
    public void enableStatistics(MBeanStatDescriptor descriptor, Set<Integer> statisticsToEnable) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to enable statistics " + statisticsToEnable + " on " + descriptor);
            }
            AdminClient adminClient = getAdminClient();
            ObjectName perfMBean = getPerfMBean();
            
            // Load the existing instrumentation level
            MBeanLevelSpec[] specs = (MBeanLevelSpec[])adminClient.invoke(perfMBean, "getInstrumentationLevel",
                    new Object[] { descriptor, Boolean.FALSE },
                    new String[] { MBeanStatDescriptor.class.getName(), Boolean.class.getName() });
            if (specs.length != 1) {
                log.error("Expected getInstrumentationLevel to return exactly one MBeanLevelSpec object");
                return;
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
                adminClient.invoke(perfMBean, "setInstrumentationLevel",
                        new Object[] { spec, Boolean.FALSE },
                        new String[] { MBeanLevelSpec.class.getName(), Boolean.class.getName() });
                
                log.info("Enabled statistics " + newStats + " on " + descriptor);
            }
        } catch (Exception ex) {
            log.error(ex); // TODO
        }
    }
    
    public ConfigDocument getConfigDocument(String uri) {
        return (ConfigDocument)configDocumentCache.get(uri).getObjectValue();
    }
}
