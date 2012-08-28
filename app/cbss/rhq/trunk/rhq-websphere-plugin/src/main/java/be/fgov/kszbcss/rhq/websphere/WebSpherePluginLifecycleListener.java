package be.fgov.kszbcss.rhq.websphere;

import java.security.Security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.plugin.PluginContext;
import org.rhq.core.pluginapi.plugin.PluginLifecycleListener;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryServiceFactory;
import be.fgov.kszbcss.rhq.websphere.connector.security.CustomProvider;
import be.fgov.kszbcss.rhq.websphere.connector.security.TrustStoreManager;

import com.ibm.ws.ssl.config.SSLConfig;
import com.ibm.ws.ssl.config.SSLConfigManager;

public class WebSpherePluginLifecycleListener implements PluginLifecycleListener {
    private static final Log log = LogFactory.getLog(WebSpherePluginLifecycleListener.class);
    
    private static final String SSL_CONFIG_ALIAS = "RHQSSLConfig";
    
    private SSLConfigManager configManager;
    private SSLConfig sslConfig;
    
    public void initialize(PluginContext context) throws Exception {
        TrustStoreManager.init(context);
        
        ConfigQueryServiceFactory.init(context);
        
        // This is obviously ugly, but we didn't find a way yet to set the CSI
        // properties dynamically (in contrast to the SSL properties)
        System.setProperty("com.ibm.CORBA.ConfigURL", WebSpherePluginLifecycleListener.class.getResource("sas.client.props").toExternalForm());
        
        // TODO: we should specify com.ibm.ssl.customTrustManagers and set com.ibm.ssl.skipDefaultTrustManagerWhenCustomDefined=true
        //       to use our own trust manager so that we can reload the trust store without restarting the agent;
        //       the TrustManagerExtendedInfo interface may also be interesting
        
        Security.addProvider(new CustomProvider());
        
        sslConfig = new SSLConfig();
        // TODO
//        sslConfig.setProperty("com.ibm.ssl.dynamicSelectionInfo", "IIOP,*,*");
        sslConfig.setProperty("com.ibm.ssl.dynamicSelectionInfo", "*,*,*");
        sslConfig.setProperty("com.ibm.ssl.trustStore", "dummy");
        sslConfig.setProperty("com.ibm.ssl.trustStorePassword", "dummy");
        // TODO: for the moment, use a dummy trust manager
        sslConfig.setProperty("com.ibm.ssl.trustManager", "Dummy|" + CustomProvider.NAME);
        
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
        ConfigQueryServiceFactory.destroy();
        TrustStoreManager.destroy();
        configManager = null;
        sslConfig = null;
        Security.removeProvider(CustomProvider.NAME);
    }
}
