package be.fgov.kszbcss.rhq.websphere.connector.security;

import java.security.KeyStore;

public interface TrustStoreAction {
    void execute(KeyStore truststore) throws Exception;
}
