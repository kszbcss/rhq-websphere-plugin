/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012 Crossroads Bank for Social Security
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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

class RefreshRequest<K,V> implements Runnable, Comparable<RefreshRequest<K,V>> {
    private final int id;
    private final long time;
    private final K key;
    private final FutureTask<V> task;
    private volatile boolean immediate;
    
    RefreshRequest(final DelayedRefreshCache<K,V> cache, int id, K key, boolean immediate) {
        this.id = id;
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
