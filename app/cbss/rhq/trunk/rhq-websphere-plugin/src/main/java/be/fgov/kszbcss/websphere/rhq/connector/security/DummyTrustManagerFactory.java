package be.fgov.kszbcss.websphere.rhq.connector.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

public class DummyTrustManagerFactory extends TrustManagerFactorySpi {
    @Override
    protected void engineInit(KeyStore arg0) throws KeyStoreException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void engineInit(ManagerFactoryParameters arg0)
            throws InvalidAlgorithmParameterException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return new TrustManager[] { new DummyTrustManager() };
    }
}
