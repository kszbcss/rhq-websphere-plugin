package be.fgov.kszbcss.rhq.websphere.connector.agent;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;

import com.ibm.websphere.management.exception.ConnectorException;

public class CellRootCertificateQuery implements ConfigQuery<X509Certificate> {
    public static final CellRootCertificateQuery INSTANCE = new CellRootCertificateQuery();
    
    private static final long serialVersionUID = -2297752052572075867L;

    private CellRootCertificateQuery() {}
    
    public X509Certificate execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException {
        for (ConfigObject keyStoreConfig : config.cell().path("Security").resolveSingle().getChildren("keyStores")) {
            if (keyStoreConfig.getAttribute("name").equals("CellDefaultTrustStore")) {
                String location = (String)keyStoreConfig.getAttribute("location");
                if (!location.startsWith("${CONFIG_ROOT}/")) {
                    // TODO: use proper exception here
                    throw new JMException("Cannot extract cell default trust store because it is not located in the configuration repository");
                }
                byte[] cellDefaultTrustStore = config.extract(location.substring(location.indexOf('/')+1));
                try {
                    KeyStore ks = KeyStore.getInstance((String)keyStoreConfig.getAttribute("type"), (String)keyStoreConfig.getAttribute("provider"));
                    ks.load(new ByteArrayInputStream(cellDefaultTrustStore), ((String)keyStoreConfig.getAttribute("password")).toCharArray());
                    return (X509Certificate)ks.getCertificate("root");
                } catch (Exception ex) {
                    // TODO: use proper exception here
                    throw new JMException("Failed to extract certificate: " + ex.getMessage());
                }
            }
        }
        // TODO: use proper exception here
        throw new JMException("CellDefaultTrustStore not found");
    }
    
    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CellRootCertificateQuery;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
