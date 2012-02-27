package be.fgov.kszbcss.rhq.websphere.component;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public final class ConnectionFactoryInfo implements Serializable {
    private static final long serialVersionUID = -5954603072274720579L;
    
    private final String id;
    private final String providerName;
    private final String name;
    private final String jndiName;
    private final String dataSourceHelper;
    private final Map<String,Object> properties;
    
    ConnectionFactoryInfo(String id, String providerName, String name, String jndiName, String dataSourceHelper, Map<String,Object> properties) {
        this.id = id;
        this.providerName = providerName;
        this.name = name;
        this.jndiName = jndiName;
        this.dataSourceHelper = dataSourceHelper;
        this.properties = properties;
    }

    public String getId() {
        return id;
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

    public String getDataSourceHelper() {
        return dataSourceHelper;
    }

    public Map<String,Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }
}
