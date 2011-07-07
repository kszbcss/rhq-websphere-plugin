package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import java.util.Collections;
import java.util.Map;

import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryInfo;

public class DataSourceInfo extends ConnectionFactoryInfo {
    private static final long serialVersionUID = -5031192955101440876L;
    
    private final Map<String,Object> properties;
    
    public DataSourceInfo(String providerName, String name, String jndiName, Map<String,Object> properties) {
        super(providerName, name, jndiName);
        this.properties = properties;
    }

    public Map<String,Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }
}
