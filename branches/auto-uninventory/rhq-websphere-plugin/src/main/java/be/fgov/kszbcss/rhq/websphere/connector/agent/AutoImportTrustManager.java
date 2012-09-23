package be.fgov.kszbcss.rhq.websphere.connector.agent;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.connector.security.TrustStoreManager;

class AutoImportTrustManager implements X509TrustManager {
    private static final Log log = LogFactory.getLog(AutoImportTrustManager.class);
    
    private final String alias;
    
    AutoImportTrustManager(String importAlias) {
        this.alias = importAlias;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        throw new UnsupportedOperationException();
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        X509Certificate cert = chain[0];
        log.info("Importing certificate for " + cert.getSubjectDN());
        try {
            TrustStoreManager.getInstance().addCertificate(alias, cert);
        } catch (Exception ex) {
            log.error("Failed to import certificate", ex);
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
