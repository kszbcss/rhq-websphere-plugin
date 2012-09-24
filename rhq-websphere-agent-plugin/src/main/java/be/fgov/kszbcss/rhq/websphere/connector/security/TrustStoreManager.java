package be.fgov.kszbcss.rhq.websphere.connector.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.plugin.PluginContext;

public class TrustStoreManager {
    private static TrustStoreManager instance;

    private static final Log log = LogFactory.getLog(TrustStoreManager.class);
    
    private final ReadWriteLock truststoreLock = new ReentrantReadWriteLock();
    private final File truststoreFile;
    private final AtomicReference<X509TrustManager> trustManager = new AtomicReference<X509TrustManager>();
    
    private TrustStoreManager(File truststoreFile) throws Exception {
        this.truststoreFile = truststoreFile;
        reloadTrustManager();
    }

    public synchronized static void init(PluginContext context) throws Exception {
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

    X509TrustManager getTrustManager() {
        return trustManager.get();
    }
    
    private void reloadTrustManager() throws GeneralSecurityException, IOException {
        if (trustManager.get() == null) {
            log.info("Initializing trust manager using " + truststoreFile);
        } else {
            log.info("Reinitializing trust manager");
        }
        TrustManagerFactory factory = TrustManagerFactory.getInstance("IbmPKIX", "IBMJSSE2");
        factory.init(loadTrustStore());
        TrustManager[] trustManagers = factory.getTrustManagers();
        if (log.isDebugEnabled()) {
            log.debug("Returned trust managers: " + Arrays.asList(trustManagers));
        }
        trustManager.set((X509TrustManager)trustManagers[0]);
    }
    
    private KeyStore loadTrustStore() throws GeneralSecurityException, IOException {
        Lock lock = truststoreLock.readLock();
        lock.lock();
        try {
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
                    log.debug("Trust store " + truststoreFile + " doesn't exist yet; a new one will be created if necessary");
                }
                truststore.load(null);
            }
            return truststore;
        } finally {
            lock.unlock();
        }
    }
    
    public void addCertificate(final String alias, final X509Certificate cert) throws Exception {
        execute(new TrustStoreAction() {
            public void execute(KeyStore truststore) throws Exception {
                truststore.setCertificateEntry(alias, cert);
            }
        }, false);
    }
    
    public void execute(TrustStoreAction action, boolean readOnly) throws Exception {
        Lock lock = readOnly ? truststoreLock.readLock() : truststoreLock.writeLock();
        lock.lock();
        try {
            KeyStore truststore = loadTrustStore();
            action.execute(truststore);
            if (!readOnly) {
                if (log.isDebugEnabled()) {
                    log.debug("Writing trust store with " + truststore.size() + " entries to " + truststoreFile);
                }
                OutputStream out = new FileOutputStream(truststoreFile);
                try {
                    truststore.store(out, new char[0]);
                } finally {
                    out.close();
                }
                reloadTrustManager();
            }
        } finally {
            lock.unlock();
        }
    }
}
