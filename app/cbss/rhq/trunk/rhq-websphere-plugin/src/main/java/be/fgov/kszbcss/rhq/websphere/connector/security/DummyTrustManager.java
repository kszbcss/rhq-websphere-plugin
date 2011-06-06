package be.fgov.kszbcss.rhq.websphere.connector.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.wsspi.ssl.TrustManagerExtendedInfo;

// TODO: as the name indicates, this is just a dummy trust manager
//       that will be used until we have a smart way to handle certificates...
public class DummyTrustManager implements X509TrustManager, TrustManagerExtendedInfo {
    private static final Log log = LogFactory.getLog(DummyTrustManager.class);

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // TODO Auto-generated method stub
        
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        log.info("Accepting server certificate for " + chain[0].getSubjectDN());
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public void setCustomProperties(Properties customProperties) {
        // TODO Auto-generated method stub
        
    }

    public void setExtendedInfo(Map info) {
        log.info("Connection info: " + info);
    }

    public void setSSLConfig(Properties config) {
        // TODO Auto-generated method stub
        
    }

}
