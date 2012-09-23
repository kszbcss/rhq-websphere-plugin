package be.fgov.kszbcss.rhq.websphere.config;

import java.io.Serializable;

// TODO: Javadoc is no longer accurate
/**
 * Supports sending queries for configuration data to a deployment manager. There is a single
 * instance of this class for each WebSphere cell with at least one server being monitored by the
 * plugin. Query results are stored in a cache.
 * <p>
 * <b>Note:</b> The implementation is designed such that a single cache instance can be used for all
 * cells (i.e. the cache key contains the cell name). This makes sure that cache entries eventually
 * disappear when all servers for a given cell are removed from the inventory.
 */
public interface ConfigQueryService {
    /**
     * 
     * 
     * @param <T>
     * @param query
     * @param immediate
     *            <code>true</code> if a stale entry in the query cache should be refreshed
     *            immediately (in which case the method waits for the completion of the refresh);
     *            <code>false</code> if the method should return the stale entry and schedule it for
     *            refresh later
     * @return
     * @throws InterruptedException
     */
    <T extends Serializable> T query(ConfigQuery<T> query, boolean immediate) throws InterruptedException;
    
    void release();
}
