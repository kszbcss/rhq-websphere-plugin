package be.fgov.kszbcss.rhq.websphere.config.cache;

public interface DelayedRefreshCacheEntryFactory<K,V> {
    boolean isStale(K key, V value);
    V createEntry(K key) throws CacheRefreshException;
}
