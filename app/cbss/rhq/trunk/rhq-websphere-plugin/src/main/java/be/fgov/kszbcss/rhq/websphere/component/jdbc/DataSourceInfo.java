package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import java.io.Serializable;

public class DataSourceInfo implements Serializable {
    private static final long serialVersionUID = -5069555839262530879L;

    private final String providerName;
    private final String name;
    private final String jndiName;
    
    public DataSourceInfo(String providerName, String name, String jndiName) {
        this.providerName = providerName;
        this.name = name;
        this.jndiName = jndiName;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getName() {
        return name;
    }

    public String getJndiName() {
        return jndiName;
    }
}
