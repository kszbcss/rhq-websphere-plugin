package be.fgov.kszbcss.websphere.rhq;

import java.util.Properties;

import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilterSupport;
import javax.management.ObjectName;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.event.EventContext;

import com.ibm.websphere.management.AdminClient;

import be.fgov.kszbcss.websphere.rhq.repository.ConfigDocument;
import be.fgov.kszbcss.websphere.rhq.repository.ConfigDocumentFactory;

public class ManagedServer extends WebSphereServer {
    private static final Log log = LogFactory.getLog(ManagedServer.class);
    
    private final Configuration config;
    private final StateChangeEventDispatcher stateEventDispatcher = new StateChangeEventDispatcher();
    private CacheManager cacheManager;
    private Ehcache configDocumentCache;
    
    public ManagedServer(String cell, String node, String server, Configuration config) {
        super(cell, node, server);
        this.config = config;
    }

    @Override
    public void init() {
        super.init();
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
    
    @Override
    protected void getAdminClientProperties(Properties properties) {
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
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        cacheManager.shutdown();
    }
    
    public void registerStateChangeEventContext(ObjectName objectNamePattern, EventContext context) {
        stateEventDispatcher.registerEventContext(objectNamePattern, context);
    }

    public void unregisterStateChangeEventContext(ObjectName objectNamePattern) {
        stateEventDispatcher.unregisterEventContext(objectNamePattern);
    }

    public ConfigDocument getConfigDocument(String uri) {
        return (ConfigDocument)configDocumentCache.get(uri).getObjectValue();
    }
}
