package be.fgov.kszbcss.rhq.websphere.config;

import net.sf.ehcache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.DeploymentManager;

/**
 * Manages the communication with the deployment manager of a given WebSphere cell. There will be
 * one instance of this class for each cell for which there is at least one monitored WebSphere
 * instance.
 */
class DeploymentManagerConnection {
    private static final Log log = LogFactory.getLog(DeploymentManagerConnection.class);

    private final ConfigQueryServiceFactory factory;
    private final ConfigQueryServiceImpl configQueryService;
    private int refCount;
    
    DeploymentManagerConnection(ConfigQueryServiceFactory factory, CacheManager cacheManager, DeploymentManager dm, String cell) {
        this.factory = factory;
        configQueryService = new ConfigQueryServiceImpl(cacheManager, cell, dm, cell);
    }
    
    ConfigQueryServiceImpl getConfigQueryService() {
        return configQueryService;
    }
    
    synchronized void incrementRefCount() {
        refCount++;
        if (log.isDebugEnabled()) {
            log.debug("New ref count is " + refCount);
        }
    }

    synchronized void decrementRefCount() {
        refCount--;
        if (log.isDebugEnabled()) {
            log.debug("New ref count is " + refCount);
        }
        if (refCount == 0) {
            log.debug("Destroying DeploymentManagerConnection");
            configQueryService.release();
            factory.removeDeploymentManagerConnection(this);
        }
    }
}
