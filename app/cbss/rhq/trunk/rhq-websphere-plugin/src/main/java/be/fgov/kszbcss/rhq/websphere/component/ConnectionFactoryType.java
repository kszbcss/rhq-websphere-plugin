package be.fgov.kszbcss.rhq.websphere.component;

public enum ConnectionFactoryType {
    JDBC("DataSource", "JDBCProvider", "JDBCProvider"),
    J2C("J2CConnectionFactory", "J2CResourceAdapter", "J2CResourceAdapter");
    
    private final String configurationObjectType;
    private final String containingConfigurationObjectType;
    private final String providerKeyProperty;
    
    private ConnectionFactoryType(String configurationObjectType, String containingConfigurationObjectType, String providerKeyProperty) {
        this.configurationObjectType = configurationObjectType;
        this.containingConfigurationObjectType = containingConfigurationObjectType;
        this.providerKeyProperty = providerKeyProperty;
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
}
