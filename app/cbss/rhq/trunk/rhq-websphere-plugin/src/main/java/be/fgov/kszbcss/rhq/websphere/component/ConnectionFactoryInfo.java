package be.fgov.kszbcss.rhq.websphere.component;

import java.io.Serializable;

public abstract class ConnectionFactoryInfo implements Serializable {
    private final String providerName;
    private final String name;
    private final String jndiName;
    
    public ConnectionFactoryInfo(String providerName, String name, String jndiName) {
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
