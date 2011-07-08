package be.fgov.kszbcss.rhq.websphere.component;

import com.ibm.websphere.pmi.PmiConstants;

public enum ConnectionFactoryType {
    JDBC("DataSource", "JDBCProvider", "JDBCProvider", PmiConstants.CONNPOOL_MODULE),
    J2C("J2CConnectionFactory", "J2CResourceAdapter", "J2CResourceAdapter", PmiConstants.J2C_MODULE);
    
    private final String configurationObjectType;
    private final String containingConfigurationObjectType;
    private final String providerKeyProperty;
    private final String pmiModule;
    
    private ConnectionFactoryType(String configurationObjectType, String containingConfigurationObjectType, String providerKeyProperty, String pmiModule) {
        this.configurationObjectType = configurationObjectType;
        this.containingConfigurationObjectType = containingConfigurationObjectType;
        this.providerKeyProperty = providerKeyProperty;
        this.pmiModule = pmiModule;
    }

    public String getConfigurationObjectType() {
        return configurationObjectType;
    }

    public String getContainingConfigurationObjectType() {
        return containingConfigurationObjectType;
    }

    public String getProviderKeyProperty() {
        return providerKeyProperty;
    }

    public String getPmiModule() {
        return pmiModule;
    }
}
