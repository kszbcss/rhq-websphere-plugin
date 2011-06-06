package be.fgov.kszbcss.rhq.websphere.config;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.plugin.PluginContext;

import be.fgov.kszbcss.rhq.websphere.DeploymentManager;

public class ConfigQueryServiceFactory {
    private static final Log log = LogFactory.getLog(ConfigQueryServiceFactory.class);
    
    private static final String CACHE_NAME = "ConfigQueryCache";
    
    private static ConfigQueryServiceFactory instance;
    
    private final ScheduledExecutorService executorService;
    private final Ehcache queryCache;
    private final Map<String,DeploymentManagerConnection> dmcMap = new HashMap<String,DeploymentManagerConnection>();
    private final CacheManager cacheManager;
    
    private ConfigQueryServiceFactory(PluginContext context) {
        log.debug("Initializing ConfigQueryServiceFactory");
        executorService = Executors.newScheduledThreadPool(2);
        Configuration config = new Configuration();
        config.setUpdateCheck(false);
        DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
        File cacheDirectory = new File(context.getDataDirectory(), "cache");
        cacheDirectory.mkdirs();
        diskStoreConfiguration.setPath(cacheDirectory.getAbsolutePath());
        config.addDiskStore(diskStoreConfiguration);
        CacheConfiguration cacheConfig = new CacheConfiguration(CACHE_NAME, 100);
        // Every time an entry is accessed, we check if it is up to date (by checking the repository epoch).
        // Therefore we really need to use timeToIdleSeconds here.
        cacheConfig.setTimeToIdleSeconds(7*24*3600);
        // This ensures persistence between agent/plugin restarts
        cacheConfig.setDiskPersistent(true);
        config.addCache(cacheConfig);
        cacheManager = CacheManager.create(config);
        queryCache = new UpdatingSelfPopulatingCache(cacheManager.getCache(CACHE_NAME), new ConfigQueryResultFactory(this));
    }
    
    private void doDestroy() {
        log.debug("Destroying ConfigQueryServiceFactory");
        executorService.shutdown();
        cacheManager.shutdown();
    }
    
    public synchronized static void init(PluginContext context) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }
        instance = new ConfigQueryServiceFactory(context);
    }
    
    public synchronized static ConfigQueryServiceFactory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Not initialized");
        }
        return instance;
    }

    public synchronized static void destroy() {
        if (instance == null) {
            throw new IllegalStateException("Not initialized");
        }
        instance.doDestroy();
        instance = null;
    }

    public synchronized ConfigQueryService getConfigQueryService(String cell, DeploymentManager deploymentManager) {
        DeploymentManagerConnection dmc = dmcMap.get(cell);
        if (dmc == null) {
            dmc = new DeploymentManagerConnection(this, deploymentManager, executorService);
            dmcMap.put(cell, dmc);
        }
        dmc.incrementRefCount();
        return new ConfigQueryService(queryCache, cell, dmc);
    }
    
    synchronized void removeDeploymentManagerConnection(DeploymentManagerConnection dmc) {
        for (Iterator<Map.Entry<String,DeploymentManagerConnection>> it = dmcMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String,DeploymentManagerConnection> entry = it.next();
            if (entry.getValue() == dmc) {
                it.remove();
                break;
            }
        }
        throw new IllegalArgumentException("Unknown connection");
    }
    
    DeploymentManagerConnection lookupDeploymentManagerConnection(String cell) {
        DeploymentManagerConnection dmc = dmcMap.get(cell);
        if (dmc == null) {
            throw new IllegalArgumentException("No deployment manager connection for cell " + cell);
        }
        return dmc;
    }
}
