package be.fgov.kszbcss.rhq.websphere.connector.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DelegatingTrustManager implements X509TrustManager {
    private static final Log log = LogFactory.getLog(DelegatingTrustManager.class);
    
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        throw new UnsupportedOperationException();
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            TrustStoreManager.getInstance().getTrustManager().checkServerTrusted(chain, authType);
            log.info("Accepted server certificate for " + chain[0].getSubjectDN());
        } catch (CertificateException ex) {
            log.error("Rejected server certificate for " + chain[0].getSubjectDN() + ": " + ex.getMessage());
            throw ex;
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return TrustStoreManager.getInstance().getTrustManager().getAcceptedIssuers();
    }
}
