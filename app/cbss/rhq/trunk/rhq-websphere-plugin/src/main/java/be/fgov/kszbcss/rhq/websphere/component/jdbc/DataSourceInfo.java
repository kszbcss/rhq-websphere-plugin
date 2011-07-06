package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class DataSourceInfo implements Serializable {
    private static final long serialVersionUID = -5031192955101440876L;
    
    private final String providerName;
    private final String name;
    private final String jndiName;
    private final Map<String,Object> properties;
    
    public DataSourceInfo(String providerName, String name, String jndiName, Map<String,Object> properties) {
        this.providerName = providerName;
        this.name = name;
        this.jndiName = jndiName;
        this.properties = properties;
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
    
    public Map<String,Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }
}
