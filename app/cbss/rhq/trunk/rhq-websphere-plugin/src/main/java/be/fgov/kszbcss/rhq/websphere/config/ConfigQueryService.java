package be.fgov.kszbcss.rhq.websphere.config;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.ehcache.Ehcache;

/**
 * Supports sending queries for configuration data to a deployment manager. There is a single
 * instance of this class for each WebSphere cell with at least one server being monitored by the
 * plugin. Query results are stored in a cache.
 * <p>
 * <b>Note:</b> The implementation is designed such that a single cache instance can be used for all
 * cells (i.e. the cache key contains the cell name). This makes sure that cache entries eventually
 * disappear when all servers for a given cell are removed from the inventory.
 */
public class ConfigQueryService {
    private static final Log log = LogFactory.getLog(ConfigQueryService.class);
    
    private final Ehcache cache;
    private final String cell;
    private DeploymentManagerConnection dmc;
    
    ConfigQueryService(Ehcache cache, String cell, DeploymentManagerConnection dmc) {
        this.cache = cache;
        this.cell = cell;
        this.dmc = dmc;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T query(ConfigQuery<T> query) {
        return (T)((ConfigQueryResult)cache.get(new ConfigQueryKey(cell, query)).getObjectValue()).object;
    }
    
    public void release() {
        if (log.isDebugEnabled()) {
            log.debug("Releasing one instance of ConfigQueryService for cell " + cell);
        }
        dmc.decrementRefCount();
        dmc = null;
    }
}
