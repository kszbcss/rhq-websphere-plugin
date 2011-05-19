package be.fgov.kszbcss.websphere.rhq.config;

import java.io.Serializable;

import com.ibm.websphere.management.configservice.ConfigService;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache;

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
    private final Ehcache cache;
    private final String cell;
    
    ConfigQueryService(Ehcache cache, String cell) {
        this.cache = cache;
        this.cell = cell;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T query(ConfigQuery<T> query) {
        return (T)cache.get(query).getObjectValue();
    }
    
    public void release() {
        // TODO
    }
}
