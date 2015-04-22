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
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.JMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.fgov.kszbcss.rhq.websphere.config.CacheRefreshStrategy;
import be.fgov.kszbcss.rhq.websphere.config.ConfigData;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryExecutor;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.management.repository.ConfigEpoch;

public class ConfigQueryCache implements Runnable {
    /**
     * The interval in milliseconds between two attempts to refresh a configuration entry.
     */
    private static final int RETRY_INTERVAL = 5*60*1000; // 5 minutes
    
	private static final Logger log = LoggerFactory.getLogger(ConfigQueryCache.class);

    private final String name;
    private final ConfigQueryExecutor queryExecutor;
    private final File persistentFile;
    private final Map<ConfigQuery<?>,ConfigQueryCacheEntry<?>> cache = new HashMap<ConfigQuery<?>,ConfigQueryCacheEntry<?>>();
    private ConfigEpoch epoch;
    private Thread[] threads;
    private boolean stopping;
    private Timer timer;
    
    public ConfigQueryCache(String name, ConfigQueryExecutor queryExecutor, File persistentFile) {
        this.name = name;
        this.queryExecutor = queryExecutor;
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
                    long currentTime = System.currentTimeMillis();
                    long wakeup = -1; //in case no current candidate to refresh, the moment when to retry first entry of which refresh failed last time
                    int maxWaiters = -1;
                    ConfigEpoch maxEpoch = null;
                    ConfigQueryCacheEntry<?> entry = null;
                    for (ConfigQueryCacheEntry<?> candidate : cache.values()) {
                        synchronized (candidate) {
                            if (!candidate.refreshing) {
                                int waiters = candidate.waitingThreads == null ? 0 : candidate.waitingThreads.size();
                                if (candidate.refCount > 0 && !epoch.equals(candidate.epoch)) {
                                    if (candidate.lastTransientError != 0 && currentTime-candidate.lastTransientError < RETRY_INTERVAL) {
                                        long t = candidate.lastTransientError + RETRY_INTERVAL;
                                        if (wakeup == -1 || t < wakeup) {
                                            wakeup = t;
                                        }
                                    } else {
                                        // Give priority first for entries with more waiting threads, then to the ones with oldest refresh time
                                        if (waiters > maxWaiters) {
                                            entry = candidate;
                                            maxWaiters = waiters;
                                            maxEpoch = candidate.epoch;
                                        } else if (waiters == maxWaiters) {
                                        	if (candidate.epoch == null && maxEpoch != null) {
                                        		entry = candidate;
                                        		maxEpoch = candidate.epoch;
                                        	} else if (candidate.epoch != null && maxEpoch != null && (candidate.epoch.compareTo(maxEpoch) < 0)) {
                                        		entry = candidate;
                                        		maxEpoch = candidate.epoch;
                                        	}
										}
                                    }
                                } else if (waiters > 0) {
                                    // If we get here, something is broken
                                    log.warn("There are threads waiting for refresh of entry " + candidate.query + ", but the entry is not scheduled for refresh");
                                }
                            }
                        }
                    }
                    if (entry == null) {
                        if (wakeup == -1) {
                            log.debug("No entries to refresh; sleeping");
                            cache.wait();
                        } else {
							log.debug("No entries to refresh; sleeping for {} ms", wakeup - currentTime);
                            cache.wait(wakeup-currentTime);
                        }
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
        ConfigEpoch epoch;
        synchronized (cache) {
            epoch = this.epoch;
        }
		log.debug("Starting to refresh cache entry for {} ; current epoch: ", entry.query, epoch);
        T result = null;
        ConfigQueryException nonTransientException = null;
        boolean transientError = false;
        try {
            result = queryExecutor.query(entry.query);
        } catch (ConfigQueryException ex) {
            nonTransientException = ex;
		} catch (RuntimeException | JMException | ConnectorException | InterruptedException ex) {
			log.warn("Query execution failed", ex);
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
            if (entry.waitingThreads != null) {
                entry.waitingThreads.clear();
            }
        }
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                ConfigQueryCacheEntry<?> entry = fetchNextEntryToRefresh();
                if (entry == null) {
                    break;
                }
                refreshEntry(entry);
            }
            log.debug("Thread stopping");
        } catch (InterruptedException ex) {
            log.debug("Thread interrupted");
            return;
		} catch (RuntimeException ex) {
            log.error("Unexpected exception", ex);
        }
    }
    
    public void start(int numThreads) {
        synchronized (cache) {
            if (threads != null || stopping) {
                // start has already been called before
                throw new IllegalStateException();
            }
            if (persistentFile.exists()) {
				log.debug("Reading persistent cache {}", persistentFile);
				try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(persistentFile))) {
                        for (int i=in.readInt(); i>0; i--) {
                            ConfigQueryCacheEntry<?> entry = (ConfigQueryCacheEntry<?>)in.readObject();
                            cache.put(entry.query, entry);
                        }
                } catch (IOException ex) {
                    log.error("Failed to read persistent cache data", ex);
                } catch (ClassNotFoundException ex) {
                    log.error("Unexpected exception", ex);
                }
            }
        }
		log.debug("Starting {} worker threads", numThreads);
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
				log.debug("New epoch: {}", epoch);
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
            synchronized (entry) {
                entry.refCount++;
            }
        }
        return new ConfigDataImpl<T>(this, entry);
    }

    public void unregisterConfigQuery(ConfigQuery<?> query) {
        synchronized (cache) {
            ConfigQueryCacheEntry<?> entry = cache.get(query);
            synchronized (entry) {
                entry.refCount--;
            }
        }
    }
    
    <T extends Serializable> T get(ConfigQueryCacheEntry<T> entry) throws InterruptedException, ConfigQueryException {
        while (true) {
            ConfigEpoch epoch;
            synchronized (cache) {
                epoch = this.epoch;
            }
            synchronized (entry) {
                if (entry.refCount <= 0) {
                    throw new IllegalStateException("refCount=" + entry.refCount);
                }
                // TODO: if epoch is null (and immediate refresh is set), shouldn't we wait for the deployment manager connection to become available?
                if (entry.epoch != null && (!CacheRefreshStrategy.isImmediateRefresh() || epoch == null || epoch.equals(entry.epoch) || entry.lastTransientError != 0)) {
					log.debug("Returning cached result for query {}", entry.query);
                    if (entry.nonTransientException != null) {
                        throw entry.nonTransientException;
                    } else {
                        return entry.result;
                    }
                } else {
					log.debug("Waiting for refresh of entry {}", entry.query);
                    if (entry.waitingThreads == null) {
                        entry.waitingThreads = new HashSet<Thread>();
                    }
                    Thread thread = Thread.currentThread();
                    entry.waitingThreads.add(thread);
                    do {
                        try {
                            entry.wait();
                        } catch (InterruptedException ex) {
                            log.warn("Interrupted; query: {}; epoch: {} (current: {})", entry.query, entry.epoch, epoch);
                            entry.waitingThreads.remove(thread);
                            throw ex;
                        }
                    } while (entry.waitingThreads.contains(thread));
                }
            }
        }
    }
    
    void persist() {
		log.debug("Persisting cache to {}", persistentFile);
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(persistentFile))) {
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
        } catch (IOException ex) {
            log.error("Failed to persist cache", ex);
        }
    }
}
