/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2014 Crossroads Bank for Social Security
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
package be.fgov.kszbcss.rhq.websphere.config;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.cache.ConfigQueryCache;
import be.fgov.kszbcss.rhq.websphere.process.WebSphereServer;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.management.repository.ConfigEpoch;

public class ConfigQueryServiceImpl implements ConfigQueryService, Runnable, ConfigQueryServiceImplMBean {
    private static final Log log = LogFactory.getLog(ConfigQueryServiceImpl.class);
    
    private final CellConfiguration config;
    private final ScheduledFuture<?> future;
    private final String cell;
    private final ConfigQueryCache queryCache;
    private final ScheduledExecutorService epochPollExecutorService;
    private ObjectInstance mbean;
    private ConfigEpoch epoch;
    private boolean polled;
    private boolean waitForConnection = true;
    private Exception lastException;

    public ConfigQueryServiceImpl(String cacheName, File persistentFile, WebSphereServer server, String cell) {
        this.cell = cell;
        config = new CellConfiguration(server, cell);
        queryCache = new ConfigQueryCache(cacheName, config, persistentFile);
        epochPollExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory(cacheName + "-epoch-poll"));
        future = epochPollExecutorService.scheduleWithFixedDelay(this, 0, 30, TimeUnit.SECONDS);
        queryCache.start(2);
        try {
            mbean = ManagementFactory.getPlatformMBeanServer().registerMBean(this, ObjectName.getInstance("rhq.websphere:type=ConfigQueryService,cell=" + cell + ",cacheName=" + cacheName));
        } catch (Throwable ex) {
            log.error("MBean registration failed", ex);
        }
    }

    public void run() {
        ConfigEpoch epoch = null;
        Exception exception = null;
        try {
            epoch = config.getRepositoryEpoch();
        } catch (Exception ex) {
            exception = ex;
        }
        synchronized (this) {
            if (this.epoch != null && exception != null) {
                log.error("Lost connection to the deployment manager for cell " + cell, exception);
            } else if (!polled && exception != null) {
                log.error("Connection to deployment manager unavailable for cell " + cell, exception);
            } else if (this.epoch == null && exception == null) {
                if (polled) {
                    log.info("Connection to deployment manager reestablished for cell " + cell);
                } else {
                    log.info("Connection to deployment manager established for cell " + cell);
                }
            } else if (this.epoch != null && epoch != null && !this.epoch.equals(epoch)) {
                if (log.isDebugEnabled()) {
                    log.debug("Epoch change detected for cell " + cell + "; old epoch: " + this.epoch + "; new epoch: " + epoch);
                }
            }
            if (epoch != null && !epoch.equals(this.epoch)) {
                // The ConfigService actually creates a workspace on the deployment manager. This workspace
                // contains copies of the configuration documents. If they change, then we need to refresh
                // the workspace. Otherwise we will not see the changes.
                config.refresh();
            }
            this.epoch = epoch;
            queryCache.setEpoch(epoch);
            lastException = exception;
            if (!polled) {
                polled = true;
                notifyAll();
            }
        }
    }
    
    // TODO: do we still need this? maybe in the query method?
    synchronized ConfigEpoch getEpoch() {
        if (!polled && waitForConnection) {
            log.debug("Waiting for connection to deployment manager for cell " + cell);
            try {
                do {
                    wait();
                } while (!polled);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                // We only wait once. If we got interrupted, then this means that there is an issue
                // with the deployment manager and we should not wait the next time getEpoch is called.
                waitForConnection = false;
            }
        }
        return epoch;
    }
    
    String getCell() {
        return cell;
    }

    synchronized CellConfiguration getCellConfiguration() {
        return config;
    }

    public <T extends Serializable> T query(ConfigQuery<T> query) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        // TODO: should we check first if the connection to the deployment manager is available?
        if (log.isDebugEnabled()) {
            log.debug("Executing query: " + query);
        }
        return query.execute(config);
    }

    public <T extends Serializable> ConfigData<T> registerConfigQuery(ConfigQuery<T> query) {
        return queryCache.registerConfigQuery(query);
    }

    public void unregisterConfigQuery(ConfigQuery<?> query) {
        queryCache.unregisterConfigQuery(query);
    }

    public void release() {
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbean.getObjectName());
        } catch (Throwable ex) {
            log.error("MBean unregistration failed", ex);
        }
        config.destroy();
        future.cancel(false);
        epochPollExecutorService.shutdownNow();
        try {
            queryCache.stop();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized String dumpLastException() {
        if (lastException == null) {
            return null;
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, false);
            lastException.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        }
    }
}
