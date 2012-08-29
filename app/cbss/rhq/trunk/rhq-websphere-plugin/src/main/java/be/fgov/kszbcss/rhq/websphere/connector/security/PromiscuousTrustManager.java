package be.fgov.kszbcss.rhq.websphere.connector.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PromiscuousTrustManager implements X509TrustManager {
    private static final Log log = LogFactory.getLog(PromiscuousTrustManager.class);
    
    private final String importAlias;
    
    public PromiscuousTrustManager(String importAlias) {
        this.importAlias = importAlias;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        throw new UnsupportedOperationException();
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        X509Certificate cert = chain[0];
        if (importAlias != null) {
            log.info("Importing certificate for " + cert.getSubjectDN());
            try {
                TrustStoreManager.getInstance().addCertificate(importAlias, cert);
            } catch (Exception ex) {
                log.error("Failed to import certificate", ex);
            }
        } else {
            log.info("Skipping certificate check for " + cert.getSubjectDN());
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
