package be.fgov.kszbcss.rhq.websphere.connector.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.plugin.PluginContext;

public class TrustStoreManager {
    private static TrustStoreManager instance;

    private static final Log log = LogFactory.getLog(TrustStoreManager.class);
    
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

    public void execute(TrustStoreAction action, boolean readOnly) throws Exception {
        KeyStore truststore = KeyStore.getInstance("JKS");
        if (truststoreFile.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading existing trust store from " + truststoreFile);
            }
            InputStream in = new FileInputStream(truststoreFile);
            try {
                truststore.load(in, new char[0]);
            } finally {
                in.close();
            }
            if (log.isDebugEnabled()) {
                log.debug("Trust store has " + truststore.size() + " existing entries");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Trust store " + truststoreFile + " doesn't exist yet; will create a new one");
            }
            truststore.load(null);
        }
        if (log.isDebugEnabled()) {
            log.debug("Writing trust store with " + truststore.size() + " entries to " + truststoreFile);
        }
        action.execute(truststore);
        if (!readOnly) {
            OutputStream out = new FileOutputStream(truststoreFile);
            try {
                truststore.store(out, new char[0]);
            } finally {
                out.close();
            }
        }
    }
}
