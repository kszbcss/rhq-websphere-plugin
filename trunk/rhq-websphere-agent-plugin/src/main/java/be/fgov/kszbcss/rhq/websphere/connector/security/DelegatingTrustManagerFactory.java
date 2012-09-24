package be.fgov.kszbcss.rhq.websphere.connector.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DelegatingTrustManagerFactory extends TrustManagerFactorySpi {
    private static final Log log = LogFactory.getLog(DelegatingTrustManagerFactory.class);
    
    @Override
    protected void engineInit(KeyStore truststore) throws KeyStoreException {
        log.debug("engineInit(KeyStore) called");
    }

    @Override
    protected void engineInit(ManagerFactoryParameters params) throws InvalidAlgorithmParameterException {
        log.debug("engineInit(ManagerFactoryParameters) called");
    }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        log.debug("engineGetTrustManagers() called");
        return new TrustManager[] { new DelegatingTrustManager() };
    }
}
