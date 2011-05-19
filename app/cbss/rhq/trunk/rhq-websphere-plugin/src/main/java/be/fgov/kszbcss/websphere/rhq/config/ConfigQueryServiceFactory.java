package be.fgov.kszbcss.websphere.rhq.config;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache;

public class ConfigQueryServiceFactory {
    private final Ehcache queryCache;
    
    public ConfigQueryServiceFactory(Ehcache backingCache) {
        queryCache = new UpdatingSelfPopulatingCache(backingCache, new ConfigQueryResultFactory(this));
        
    }

    DeploymentManagerConnection lookupDeploymentManagerConnection(String cell) {
        // TODO Auto-generated method stub
        return null;
    }
}
