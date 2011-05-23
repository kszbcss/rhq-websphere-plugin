package be.fgov.kszbcss.websphere.rhq.connector.security;

import java.io.File;

import org.rhq.core.pluginapi.plugin.PluginContext;

public class TrustStoreManager {
    private static TrustStoreManager instance;
    
    private final File truststoreFile;
    
    private TrustStoreManager(File truststoreFile) {
        this.truststoreFile = truststoreFile;
    }

    public synchronized static void init(PluginContext context) {
        File dataDirectory = context.getDataDirectory();
        dataDirectory.mkdirs();
        instance = new TrustStoreManager(new File(dataDirectory, "trust.jks"));
    }
    
    public synchronized static TrustStoreManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Not initialized");
        }
        return instance;
    }

    public synchronized static void destroy() {
        instance = null;
    }

    public File getTruststoreFile() {
        return truststoreFile;
    }
}
