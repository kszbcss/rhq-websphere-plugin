package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import java.io.Serializable;

public class DataSources implements Serializable {
    private static final long serialVersionUID = 52316569380056187L;
    
    private final DataSourceInfo[] dataSources;

    public DataSources(DataSourceInfo[] dataSources) {
        this.dataSources = dataSources;
    }
    
    public String[] getJndiNames() {
        String[] result = new String[dataSources.length];
        for (int i=0; i<dataSources.length; i++) {
            result[i] = dataSources[i].getJndiName();
        }
        return result;
    }
    
    public DataSourceInfo getByJndiName(String jndiName) {
        for (DataSourceInfo dataSource : dataSources) {
            if (dataSource.getJndiName().equals(jndiName)) {
                return dataSource;
            }
        }
        return null;
    }
}
