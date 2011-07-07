package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryInfo;
import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryMBeanLocator;

import com.ibm.websphere.management.exception.ConnectorException;

public class DataSourceMBeanLocator extends ConnectionFactoryMBeanLocator {
    public DataSourceMBeanLocator(String jndiName) {
        super(jndiName);
    }

    @Override
    protected ConnectionFactoryInfo getConnectionFactoryInfo(ManagedServer server, String jndiName) throws JMException, ConnectorException {
        return server.queryConfig(new DataSourceQuery(server.getNode(), server.getServer())).getByJndiName(jndiName);
    }

    @Override
    protected String getType() {
        return "DataSource";
    }

    @Override
    protected String getProviderKeyProperty() {
        return "JDBCProvider";
    }
}
