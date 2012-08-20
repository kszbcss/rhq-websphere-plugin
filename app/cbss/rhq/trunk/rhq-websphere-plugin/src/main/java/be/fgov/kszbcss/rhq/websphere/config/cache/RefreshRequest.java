package be.fgov.kszbcss.rhq.websphere.config.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

class RefreshRequest<K,V> implements Runnable, Comparable<RefreshRequest<K,V>> {
    private static final AtomicInteger currentId = new AtomicInteger();
    
    private final int id;
    private final long time;
    private final K key;
    private final FutureTask<V> task;
    private volatile boolean immediate;
    
    RefreshRequest(final DelayedRefreshCache<K,V> cache, K key, boolean immediate) {
        id = currentId.incrementAndGet();
        time = System.currentTimeMillis();
        this.key = key;
        this.immediate = immediate;
        task = new FutureTask<V>(new Callable<V>() {
            public V call() throws Exception {
                return cache.execute(RefreshRequest.this);
            }
        });
    }
    
    int getId() {
        return id;
    }

    long getTime() {
        return time;
    }

    K getKey() {
        return key;
    }

    V getValue() throws InterruptedException, CacheRefreshException {
        try {
            return task.get();
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof CacheRefreshException) {
                throw (CacheRefreshException)cause;
            } else {
                throw new RuntimeException("Unexpected exception", cause);
            }
        }
    }

    boolean isImmediate() {
        return immediate;
    }

    void setImmediate() {
        this.immediate = true;
    }

    public void run() {
        task.run();
    }

    public int compareTo(RefreshRequest<K,V> o) {
        int c = (immediate ? 0 : 1) - (o.immediate ? 0 : 1);
        if (c != 0) {
            return c;
        } else {
            long l = time-o.time;
            if (l < 0) {
                return -1;
            } else if (l > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
