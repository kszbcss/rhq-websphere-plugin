package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryDiscoveryComponent;
import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryType;

public class DataSourceDiscoveryComponent extends ConnectionFactoryDiscoveryComponent {
    @Override
    protected ConnectionFactoryType getType() {
        return ConnectionFactoryType.JDBC;
    }

    @Override
    protected String getDescription() {
        return "A data source";
    }
}
