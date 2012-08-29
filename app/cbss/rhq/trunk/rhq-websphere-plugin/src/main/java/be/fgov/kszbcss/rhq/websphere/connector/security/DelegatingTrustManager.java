package be.fgov.kszbcss.rhq.websphere.connector.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DelegatingTrustManager implements X509TrustManager {
    private static final Log log = LogFactory.getLog(DelegatingTrustManager.class);
    
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        TrustStoreManager.getInstance().getTrustManager().checkClientTrusted(chain, authType);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        TrustStoreManager trustStoreManager = TrustStoreManager.getInstance();
        if (trustStoreManager.isCertificateCheckEnabled()) {
            try {
                trustStoreManager.getTrustManager().checkServerTrusted(chain, authType);
                log.info("Accepted server certificate for " + chain[0].getSubjectDN());
            } catch (CertificateException ex) {
                log.error("Rejected server certificate for " + chain[0].getSubjectDN() + ": " + ex.getMessage());
                throw ex;
            }
        } else {
            log.warn("Skipping certificate check for " + chain[0].getSubjectDN());
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return TrustStoreManager.getInstance().getTrustManager().getAcceptedIssuers();
    }
}
