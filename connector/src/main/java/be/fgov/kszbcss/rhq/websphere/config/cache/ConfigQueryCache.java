/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2014 Crossroads Bank for Social Security
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package be.fgov.kszbcss.rhq.websphere.config.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.CacheRefreshStrategy;
import be.fgov.kszbcss.rhq.websphere.config.ConfigData;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryService;

import com.ibm.websphere.management.repository.ConfigEpoch;

public class ConfigQueryCache implements Runnable {
    /**
     * The interval in milliseconds between two attempts to refresh a configuration entry.
     */
    private static final int RETRY_INTERVAL = 5*60*1000; // 5 minutes
    
    private static final Log log = LogFactory.getLog(ConfigQueryCache.class);

    private final String name;
    private final ConfigQueryService queryService;
    private final File persistentFile;
    private final Map<ConfigQuery<?>,ConfigQueryCacheEntry<?>> cache = new HashMap<ConfigQuery<?>,ConfigQueryCacheEntry<?>>();
    private ConfigEpoch epoch;
    private Thread[] threads;
    private boolean stopping;
    private Timer timer;
    
    public ConfigQueryCache(String name, ConfigQueryService queryService, File persistentFile) {
        this.name = name;
        this.queryService = queryService;
        this.persistentFile = persistentFile;
    }

    private ConfigQueryCacheEntry<?> fetchNextEntryToRefresh() throws InterruptedException {
        synchronized (cache) {
            while (true) {
                if (stopping) {
                    return null;
                }
                if (epoch == null) {
                    log.debug("Repository epoch is unknown (no connection to the deployment manager?); sleeping");
                    cache.wait();
                } else {
                    int maxWaiters = -1;
                    ConfigQueryCacheEntry<?> entry = null;
                    for (ConfigQueryCacheEntry<?> candidate : cache.values()) {
                        synchronized (candidate) {
                            if (!candidate.refreshing && candidate.refCount > 0 && !epoch.equals(candidate.epoch)
                                    && (candidate.lastTransientError == 0 || System.currentTimeMillis()-candidate.lastTransientError > RETRY_INTERVAL)) {
                                if (candidate.waiters > maxWaiters) {
                                    entry = candidate;
                                    maxWaiters = candidate.waiters;
                                }
                            } else if (candidate.waiters > 0) {
                                // If we get here, something is broken
                                log.warn("There are threads waiting for refresh of entry " + candidate.query + ", but the entry is not scheduled for refresh");
                            }
                        }
                    }
                    if (entry == null) {
                        log.debug("No entries to refresh; sleeping");
                        cache.wait();
                    } else {
                        synchronized (entry) {
                            // We kept the lock on cache. Therefore refreshing must still be false.
                            entry.refreshing = true;
                            return entry;
                        }
                    }
                }
            }
        }
    }
    
    private <T extends Serializable> void refreshEntry(ConfigQueryCacheEntry<T> entry) throws InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Starting to refresh cache entry for " + entry.query);
        }
        ConfigEpoch epoch;
        synchronized (cache) {
            epoch = this.epoch;
        }
        T result = null;
        ConfigQueryException nonTransientException = null;
        boolean transientError = false;
        try {
            result = queryService.query(entry.query);
        } catch (ConfigQueryException ex) {
            nonTransientException = ex;
        } catch (Throwable ex) {
            log.debug("Query execution failed", ex);
            transientError = true;
        }
        synchronized (entry) {
            if (transientError) {
                entry.lastTransientError = System.currentTimeMillis();
            } else {
                entry.epoch = epoch;
                entry.result = result;
                entry.nonTransientException = nonTransientException;
                entry.lastTransientError = 0;
            }
            entry.refreshing = false;
            entry.notifyAll();
        }
    }
    
    public void run() {
        try {
            while (true) {
                ConfigQueryCacheEntry<?> entry = fetchNextEntryToRefresh();
                if (entry == null) {
                    break;
                }
                refreshEntry(entry);
            }
        } catch (InterruptedException ex) {
            return;
        }
    }
    
    public void start(int numThreads) {
        if (persistentFile.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Reading persistent cache " + persistentFile);
            }
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(persistentFile));
                try {
                    synchronized (cache) {
                        for (int i=in.readInt(); i>0; i--) {
                            ConfigQueryCacheEntry<?> entry = (ConfigQueryCacheEntry<?>)in.readObject();
                            cache.put(entry.query, entry);
                        }
                    }
                } finally {
                    in.close();
                }
            } catch (IOException ex) {
                log.error("Failed to read persistent cache data", ex);
            } catch (ClassNotFoundException ex) {
                log.error("Unexpected exception", ex);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Starting " + numThreads + " worker threads");
        }
        threads = new Thread[numThreads];
        for (int i=0; i<numThreads; i++) {
            Thread thread = new Thread(this, name + "-query-" + (i+1));
            threads[i] = thread;
            thread.start();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                persist();
            }
        }, 5*60*1000);
        // TODO: need another timer that removes entries that are no longer used!
    }
    
    public void stop() throws InterruptedException {
        timer.cancel();
        synchronized (cache) {
            stopping = true;
            cache.notifyAll();
        }
        for (int i=0; i<threads.length; i++) {
            threads[i].join();
        }
        threads = null;
        log.debug("Worker threads stopped");
        persist();
        synchronized (cache) {
            cache.clear();
        }
    }
    
    public void setEpoch(ConfigEpoch epoch) {
        synchronized (cache) {
            if (this.epoch == null && epoch != null || this.epoch != null && !this.epoch.equals(epoch)) {
                if (log.isDebugEnabled()) {
                    log.debug("New epoch: " + epoch);
                }
                this.epoch = epoch;
                cache.notifyAll();
            }
        }
    }

    public <T extends Serializable> ConfigData<T> registerConfigQuery(ConfigQuery<T> query) {
        ConfigQueryCacheEntry<T> entry;
        synchronized (cache) {
            entry = (ConfigQueryCacheEntry<T>)cache.get(query);
            if (entry == null) {
                entry = new ConfigQueryCacheEntry<T>(query);
                cache.put(query, entry);
            }
            entry.refCount++;
        }
        return new ConfigDataImpl<T>(this, entry);
    }

    public void unregisterConfigQuery(ConfigQuery<?> query) {
        synchronized (cache) {
            cache.get(query).refCount--;
        }
    }
    
    <T extends Serializable> T get(ConfigQueryCacheEntry<T> entry) throws InterruptedException, ConfigQueryException {
        while (true) {
            ConfigEpoch epoch;
            synchronized (cache) {
                epoch = this.epoch;
            }
            synchronized (entry) {
                // TODO: if epoch is null (and immediate refresh is set), shouldn't we wait for the deployment manager connection to become available?
                if (entry.epoch != null && (!CacheRefreshStrategy.isImmediateRefresh() || epoch == null || epoch.equals(entry.epoch) || entry.lastTransientError != 0)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Returning cached result for query " + entry.query);
                    }
                    if (entry.nonTransientException != null) {
                        throw entry.nonTransientException;
                    } else {
                        return entry.result;
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Waiting for refresh of entry " + entry.query);
                    }
                    entry.waiters++;
                    try {
                        entry.wait();
                    } finally {
                        entry.waiters--;
                    }
                }
            }
        }
    }
    
    void persist() {
        if (log.isDebugEnabled()) {
            log.debug("Persisting cache to " + persistentFile);
        }
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(persistentFile));
            try {
                ConfigQueryCacheEntry<?>[] entries;
                synchronized (cache) {
                    entries = cache.values().toArray(new ConfigQueryCacheEntry<?>[cache.size()]);
                }
                out.writeInt(entries.length);
                for (ConfigQueryCacheEntry<?> entry : entries) {
                    synchronized (entry) {
                        out.writeObject(entry);
                    }
                }
            } finally {
                out.close();
            }
        } catch (IOException ex) {
            log.error("Failed to persist cache", ex);
        }
    }
}
