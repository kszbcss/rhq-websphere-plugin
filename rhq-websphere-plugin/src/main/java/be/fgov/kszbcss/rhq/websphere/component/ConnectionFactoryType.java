package be.fgov.kszbcss.rhq.websphere.component;

import com.ibm.websphere.pmi.PmiConstants;

public enum ConnectionFactoryType {
    JDBC("DataSource", "JDBCProvider", PmiConstants.CONNPOOL_MODULE),
    J2C("J2CConnectionFactory", "J2CResourceAdapter", PmiConstants.J2C_MODULE);
    
    private final String configurationObjectType;
    private final String containingConfigurationObjectType;
    private final String pmiModule;
    
    private ConnectionFactoryType(String configurationObjectType, String containingConfigurationObjectType, String pmiModule) {
        this.configurationObjectType = configurationObjectType;
        this.containingConfigurationObjectType = containingConfigurationObjectType;
        this.pmiModule = pmiModule;
    }

    public String getConfigurationObjectType() {
        return configurationObjectType;
    }

    public String getContainingConfigurationObjectType() {
        return containingConfigurationObjectType;
    }

    public String getPmiModule() {
        return pmiModule;
    }
}
