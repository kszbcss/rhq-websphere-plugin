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
package be.fgov.kszbcss.rhq.websphere.config;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.plugin.PluginContext;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.DeploymentManager;
import be.fgov.kszbcss.rhq.websphere.UnmanagedServer;
import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

public class ConfigQueryServiceFactory {
    private static final Log log = LogFactory.getLog(ConfigQueryServiceFactory.class);
    
    private static ConfigQueryServiceFactory instance;
    
    private final Map<String,DeploymentManagerConnection> dmcMap = new HashMap<String,DeploymentManagerConnection>();
    private final CacheManager cacheManager;
    
    private ConfigQueryServiceFactory(PluginContext context) {
        log.debug("Initializing ConfigQueryServiceFactory");
        Configuration config = new Configuration();
        config.setUpdateCheck(false);
        DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
        File cacheDirectory = new File(context.getDataDirectory(), "cache");
        cacheDirectory.mkdirs();
        diskStoreConfiguration.setPath(cacheDirectory.getAbsolutePath());
        config.addDiskStore(diskStoreConfiguration);
        CacheConfiguration cacheConfig = new CacheConfiguration("default", 100);
        // Every time an entry is accessed, we check if it is up to date (by checking the repository epoch).
        // Therefore we really need to use timeToIdleSeconds here.
        cacheConfig.setTimeToIdleSeconds(7*24*3600);
        // This ensures persistence between agent/plugin restarts
        cacheConfig.setDiskPersistent(true);
        config.setDefaultCacheConfiguration(cacheConfig);
        cacheManager = new CacheManager(config);
    }
    
    private void doDestroy() {
        log.debug("Destroying ConfigQueryServiceFactory");
        cacheManager.shutdown();
    }
    
    public synchronized static void init(PluginContext context) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }
        instance = new ConfigQueryServiceFactory(context);
    }
    
    public synchronized static ConfigQueryServiceFactory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Not initialized");
        }
        return instance;
    }

    public synchronized static void destroy() {
        if (instance == null) {
            throw new IllegalStateException("Not initialized");
        }
        instance.doDestroy();
        instance = null;
    }

    public synchronized ConfigQueryService getConfigQueryService(DeploymentManager deploymentManager) throws ConnectorException {
        // Normally at this point the cell should be known (because the DeploymentManager object is
        // created from an ApplicationServer object with a known cell name). Therefore the
        // invocation of getCell should not trigger a call to the deployment manager, i.e. this
        // method returns successfully even if the deployment manager is unavailable (which is a requirement).
        String cell = deploymentManager.getCell();
        DeploymentManagerConnection dmc = dmcMap.get(cell);
        if (dmc == null) {
            dmc = new DeploymentManagerConnection(this, cacheManager, deploymentManager, cell);
            dmcMap.put(cell, dmc);
        }
        dmc.incrementRefCount();
        return new ConfigQueryServiceHandle(dmc);
    }
    
    public ConfigQueryService getConfigQueryService(UnmanagedServer server) throws ConnectorException {
        // We use cell+node+server as cache name because for a stand-alone server it is more likely that the cell name is not unique
        String cell = server.getCell();
        return new ConfigQueryServiceImpl(cacheManager, cell + "_" + server.getNode() + "_" + server.getServer(), server, cell);
    }
    
    public ConfigQueryService getConfigQueryServiceWithoutCaching(WebSphereServer server) throws ConnectorException {
        String cell = server.getCell();
        Configuration config = new Configuration();
        config.setUpdateCheck(false);
        CacheConfiguration cacheConfig = new CacheConfiguration("non-persistent", 100);
        cacheConfig.setTimeToIdleSeconds(7*24*3600);
        config.setDefaultCacheConfiguration(cacheConfig);
        final CacheManager nonPersistentCacheManager = new CacheManager(config);
        return new ConfigQueryServiceImpl(nonPersistentCacheManager, cell + "-non-persistent", server, cell) {
            @Override
            public void release() {
                super.release();
                nonPersistentCacheManager.shutdown();
            }
        };
    }
    
    synchronized void removeDeploymentManagerConnection(DeploymentManagerConnection dmc) {
        for (Iterator<Map.Entry<String,DeploymentManagerConnection>> it = dmcMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String,DeploymentManagerConnection> entry = it.next();
            if (entry.getValue() == dmc) {
                it.remove();
                return;
            }
        }
        throw new IllegalArgumentException("Unknown connection");
    }
    
    DeploymentManagerConnection lookupDeploymentManagerConnection(String cell) {
        DeploymentManagerConnection dmc = dmcMap.get(cell);
        if (dmc == null) {
            throw new IllegalArgumentException("No deployment manager connection for cell " + cell);
        }
        return dmc;
    }
}
