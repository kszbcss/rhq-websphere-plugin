package be.fgov.kszbcss.rhq.cert;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.enterprise.server.plugin.pc.content.ContentProvider;
import org.rhq.enterprise.server.plugin.pc.content.ContentProviderPackageDetails;
import org.rhq.enterprise.server.plugin.pc.content.ContentProviderPackageDetailsKey;
import org.rhq.enterprise.server.plugin.pc.content.PackageSource;
import org.rhq.enterprise.server.plugin.pc.content.PackageSyncReport;
import org.rhq.enterprise.server.plugin.pc.content.RepoDetails;
import org.rhq.enterprise.server.plugin.pc.content.RepoImportReport;
import org.rhq.enterprise.server.plugin.pc.content.RepoSource;
import org.rhq.enterprise.server.plugin.pc.content.SyncException;
import org.rhq.enterprise.server.plugin.pc.content.SyncProgressWeight;

import be.fgov.kszbcss.rhq.cert.util.CertContentConstants;
import be.fgov.kszbcss.rhq.cert.util.CertContentUtils;

public class TrustStoreSource implements ContentProvider, PackageSource, RepoSource {
    private static final Log log = LogFactory.getLog(TrustStoreSource.class);
    
    private static final Map<String,String> suffixToTypeMap = new HashMap<String,String>();
    
    static {
        suffixToTypeMap.put("jks", "JKS");
        suffixToTypeMap.put("p12", "PKCS12");
    }
    
    private File rootDirectory;
    
    public void initialize(Configuration configuration) throws Exception {
        rootDirectory = new File(((PropertySimple)configuration.get("rootDirectory")).getStringValue());
        testConnection();
    }

    public void testConnection() throws Exception {
        if (!rootDirectory.exists()) {
            throw new FileNotFoundException(rootDirectory + " not found");
        }
        if (!rootDirectory.isDirectory()) {
            throw new Exception(rootDirectory + " is not a directory");
        }
        if (!rootDirectory.canRead()) {
            throw new Exception(rootDirectory + " not readable");
        }
    }

    public RepoImportReport importRepos() throws Exception {
        RepoImportReport report = new RepoImportReport();
        for (File file : rootDirectory.listFiles()) {
            if (file.isDirectory()) {
                report.addRepo(new RepoDetails(file.getName()));
            }
        }
        return report;
    }

    public SyncProgressWeight getSyncProgressWeight() {
        // TODO: don't really know what this means; copied from DiskSource
        return new SyncProgressWeight(10, 1, 0, 0, 0);
    }
    
    private String getStoreType(String name) {
        int idx = name.lastIndexOf('.');
        return idx == -1 ? null : suffixToTypeMap.get(name.substring(idx+1));
    }
    
    private KeyStore loadTrustStore(File truststoreFile) throws Exception {
        String storeType = getStoreType(truststoreFile.getName());
        if (storeType == null) {
            throw new IllegalArgumentException("Unable to determine type of trust store " + truststoreFile);
        }
        if (log.isDebugEnabled()) {
            log.debug("Loading trust store " + truststoreFile);
        }
        KeyStore truststore = KeyStore.getInstance(storeType);
        InputStream in = new FileInputStream(truststoreFile);
        try {
            truststore.load(in, null);
        } finally {
            in.close();
        }
        return truststore;
    }
    
    public void synchronizePackages(String repoName, PackageSyncReport report,
            Collection<ContentProviderPackageDetails> existingPackages) throws SyncException, InterruptedException {
        List<ContentProviderPackageDetails> packages = new ArrayList<ContentProviderPackageDetails>();
        File file = new File(rootDirectory, repoName);
        if (file.isDirectory()) {
            discoverPackagesFromDirectory(file, packages);
        } else {
            discoverPackagesFromTrustStore(file, packages);
        }
        
        Map<ContentProviderPackageDetailsKey,ContentProviderPackageDetails> packageMap = new HashMap<ContentProviderPackageDetailsKey,ContentProviderPackageDetails>();
        for (ContentProviderPackageDetails pkg : existingPackages) {
            packageMap.put((ContentProviderPackageDetailsKey)pkg.getKey(), pkg);
        }
        for (ContentProviderPackageDetails pkg : packages) {
            ContentProviderPackageDetailsKey key = (ContentProviderPackageDetailsKey)pkg.getKey();
            if (packageMap.remove(key) == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Discovered new package " + key);
                }
                report.addNewPackage(pkg);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Package " + key + " already exists");
                }
            }
        }
        for (ContentProviderPackageDetails pkg : packageMap.values()) {
            report.addDeletePackage(pkg);
        }
    }
    
    private ContentProviderPackageDetails createContentProviderPackageDetails(X509Certificate cert) {
        String name = CertContentUtils.getPackageName(cert);
        ContentProviderPackageDetails pkg = new ContentProviderPackageDetails(new ContentProviderPackageDetailsKey(name,
                CertContentUtils.getVersion(cert), CertContentConstants.PACKAGE_TYPE_NAME, CertContentConstants.ARCHITECTURE_NAME,
                CertContentConstants.RESOURCE_TYPE_NAME, CertContentConstants.RESOURCE_TYPE_PLUGIN_NAME));
        pkg.setDisplayName(name);
        return pkg;
    }
    
    private void discoverPackagesFromDirectory(File dir, List<ContentProviderPackageDetails> packages) throws SyncException, InterruptedException {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (String fileName : dir.list()) {
                X509Certificate cert;
                FileInputStream in = new FileInputStream(new File(dir, fileName));
                try {
                    cert = (X509Certificate)cf.generateCertificate(in);
                } finally {
                    in.close();
                }
                ContentProviderPackageDetails pkg = createContentProviderPackageDetails(cert);
                // TODO: here we could set more metadata
                pkg.setFileName(fileName);
                pkg.setLocation(dir.getName() + "/" + fileName);
                packages.add(pkg);
            }
        } catch (Exception ex) {
            log.error("Exception while processing directory " + dir, ex);
            throw new SyncException("Failed to load certificates from directory " + dir, ex);
        }
    }
    
    private void discoverPackagesFromTrustStore(File truststoreFile, List<ContentProviderPackageDetails> packages) throws SyncException, InterruptedException {
        try {
            KeyStore truststore = loadTrustStore(truststoreFile);
            for (Enumeration<String> aliases = truststore.aliases(); aliases.hasMoreElements(); ) {
                String alias = aliases.nextElement();
                if (log.isDebugEnabled()) {
                    log.debug("Loading entry with alias " + alias);
                }
                X509Certificate cert = (X509Certificate)truststore.getCertificate(alias);
                ContentProviderPackageDetails pkg = createContentProviderPackageDetails(cert);
                pkg.setFileName(alias);
                pkg.setLocation(truststoreFile.getName() + "#" + alias);
                packages.add(pkg);
            }
        } catch (Exception ex) {
            log.error("Exception while processing trust store", ex);
            throw new SyncException("Failed to load key store", ex);
        }
    }

    public InputStream getInputStream(String location) throws Exception {
        int idx = location.indexOf('#');
        if (idx == -1) {
            return new FileInputStream(new File(rootDirectory, location));
        } else {
            KeyStore truststore = loadTrustStore(new File(rootDirectory, location.substring(0, idx)));
            if (log.isDebugEnabled()) {
                log.debug("Loading certificate with alias " + location);
            }
            X509Certificate cert = (X509Certificate)truststore.getCertificate(location.substring(idx+1));
            return new ByteArrayInputStream(cert.getEncoded());
        }
    }

    public void shutdown() {
    }
}
