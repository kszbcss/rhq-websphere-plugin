package be.fgov.kszbcss.rhq.websphere.config.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

public class DelayedRefreshCache<K,V> {
    private static final Log log = LogFactory.getLog(DelayedRefreshCache.class);
    
    private final Ehcache underlyingCache;
    private final ExecutorService executorService;
    private final DelayedRefreshCacheEntryFactory<K,V> entryFactory;
    private final Map<K,RefreshRequest<K,V>> pendingRequests = new HashMap<K,RefreshRequest<K,V>>();
    
    public DelayedRefreshCache(Ehcache underlyingCache, ExecutorService executorService, DelayedRefreshCacheEntryFactory<K,V> entryFactory) {
        this.underlyingCache = underlyingCache;
        this.executorService = executorService;
        this.entryFactory = entryFactory;
    }
    
    public V get(K key, boolean immediate) throws InterruptedException, CacheRefreshException {
        V value;
        RefreshRequest<K,V> refreshRequest;
        synchronized (this) {
            Element element = underlyingCache.get(key);
            value = element == null ? null : (V)element.getObjectValue();
            if (value == null || entryFactory.isStale(key, value)) {
                refreshRequest = pendingRequests.get(key);
                if (refreshRequest == null) {
                    refreshRequest = new RefreshRequest<K,V>(this, key, immediate);
                    if (log.isDebugEnabled()) {
                        log.debug("Scheduling refresh request " + refreshRequest.getId() + " (immediate=" + refreshRequest.isImmediate() + ")");
                    }
                    pendingRequests.put(key, refreshRequest);
                    executorService.submit(refreshRequest);
                } else if (immediate) {
                    refreshRequest.setImmediate();
                }
            } else {
                // The cache is up-to-date
                return value;
            }
        }
        if (value == null || immediate) {
            if (log.isDebugEnabled()) {
                log.debug("Waiting for completion of refresh request " + refreshRequest.getId());
            }
            return refreshRequest.getValue();
        } else {
            // Return the stale value
            return value;
        }
    }
    
    V execute(RefreshRequest<K,V> refreshRequest) throws CacheRefreshException {
        if (log.isDebugEnabled()) {
            long delay = System.currentTimeMillis() - refreshRequest.getTime();
            log.debug("Executing refresh request " + refreshRequest.getId() + " (delay=" + delay + "; immediate=" + refreshRequest.isImmediate() + ")");
        }
        K key = refreshRequest.getKey();
        V value = entryFactory.createEntry(key);
        synchronized (this) {
            underlyingCache.put(new Element(key, value));
            pendingRequests.remove(refreshRequest);
        }
        return value;
    }
}
