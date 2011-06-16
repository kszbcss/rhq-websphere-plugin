package be.fgov.kszbcss.rhq.websphere.component.j2c;

import java.io.Serializable;

public class ConnectionFactoryInfo implements Serializable {
    private static final long serialVersionUID = 116695055256735899L;
    
    private final String providerName;
    private final String dataSourceName;
    private final String jndiName;
    
    public ConnectionFactoryInfo(String providerName, String dataSourceName, String jndiName) {
        this.providerName = providerName;
        this.dataSourceName = dataSourceName;
        this.jndiName = jndiName;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public String getJndiName() {
        return jndiName;
    }
}
