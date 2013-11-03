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
package be.fgov.kszbcss.rhq.websphere;

import java.io.File;
import java.lang.reflect.Field;
import java.security.Security;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.plugin.PluginContext;
import org.rhq.core.pluginapi.plugin.PluginLifecycleListener;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryServiceFactory;
import be.fgov.kszbcss.rhq.websphere.connector.security.CustomProvider;
import be.fgov.kszbcss.rhq.websphere.connector.security.TrustStoreManager;

import com.ibm.CORBA.iiop.ORB;
import com.ibm.ws.orb.GlobalORBFactory;
import com.ibm.ws.ssl.config.SSLConfig;
import com.ibm.ws.ssl.config.SSLConfigManager;

public class WebSpherePluginLifecycleListener implements PluginLifecycleListener {
    private static final Log log = LogFactory.getLog(WebSpherePluginLifecycleListener.class);
    
    private static final String SSL_CONFIG_ALIAS = "RHQSSLConfig";
    
    private ORB orb;
    private SSLConfigManager configManager;
    private SSLConfig sslConfig;
    
    public void initialize(PluginContext context) throws Exception {
        // We explicitly manage the lifecycle of the ORB so that we can configure
        // it without relying on system properties and also cleanly shut it down.
        log.info("Starting ORB");
        // The ORB initialization may change the name of the current thread
        // (apparently this occurs only for the "main" thread). We don't want that.
        String threadName = Thread.currentThread().getName();
        try {
            Properties orbProps = new Properties();
            orbProps.setProperty("com.ibm.CORBA.ConfigURL", WebSpherePluginLifecycleListener.class.getResource("sas.client.props").toExternalForm());
            // This prevents the ORB from creating orbtrc files
            orbProps.setProperty("com.ibm.CORBA.Debug.Output", File.separatorChar == '/' ? "/dev/null" : "NUL");
            // Set a reasonable connection timeout. This is important when starting
            // the RHQ agent while some servers are down (and don't reply to SYN packets).
            orbProps.setProperty("com.ibm.CORBA.ConnectTimeout", "5");
            orb = GlobalORBFactory.init(new String[0], orbProps);
        } finally {
            Thread.currentThread().setName(threadName);
        }
        
        TrustStoreManager.init(context);
        
        ConfigQueryServiceFactory.init(context);
        
        // TODO: we should specify com.ibm.ssl.customTrustManagers and set com.ibm.ssl.skipDefaultTrustManagerWhenCustomDefined=true
        //       to use our own trust manager so that we can reload the trust store without restarting the agent;
        //       the TrustManagerExtendedInfo interface may also be interesting
        
        Security.addProvider(new CustomProvider());
        
        sslConfig = new SSLConfig();
        sslConfig.setProperty("com.ibm.ssl.dynamicSelectionInfo", "*,*,*");
        sslConfig.setProperty("com.ibm.ssl.trustStore", "dummy");
        sslConfig.setProperty("com.ibm.ssl.trustStorePassword", "dummy");
        sslConfig.setProperty("com.ibm.ssl.trustManager", "Delegating|" + CustomProvider.NAME);
        
        configManager = SSLConfigManager.getInstance();
        configManager.addSSLConfigToMap(SSL_CONFIG_ALIAS, sslConfig);
    }

    public void shutdown() {
        try {
            configManager.removeSSLConfigFromMap(SSL_CONFIG_ALIAS, sslConfig);
        } catch (Exception ex) {
            log.error("Unable to remove SSL configuration", ex);
        }
        System.getProperties().remove("com.ibm.CORBA.ConfigURL");
        System.getProperties().remove("com.ibm.ssl.defaultAlias");
        ConfigQueryServiceFactory.destroy();
        TrustStoreManager.destroy();
        configManager = null;
        sslConfig = null;
        Security.removeProvider(CustomProvider.NAME);
        
        // Shut down the ORB to prevent class loader leaks and to avoid reconnection
        // issues if the plugin is restarted later.
        log.info("Shutting down ORB");
        orb.shutdown(false);
        orb = null;
        // Also reset the ORB singleton holder; otherwise we will have an issue if the
        // plugin is restarted later (and the GlobalORBFactory class is loaded from the
        // system class loader).
        synchronized (GlobalORBFactory.class) {
            try {
                Field orbField = GlobalORBFactory.class.getDeclaredField("orb");
                orbField.setAccessible(true);
                orbField.set(null, null);
            } catch (Exception ex) {
                log.error("Failed to remove singleton ORB instance; this will cause a failure to restart the plugin", ex);
            }
        }
    }
}
