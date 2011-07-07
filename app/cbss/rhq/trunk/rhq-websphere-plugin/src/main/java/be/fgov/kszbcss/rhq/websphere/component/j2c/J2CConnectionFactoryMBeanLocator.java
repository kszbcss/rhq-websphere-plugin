package be.fgov.kszbcss.rhq.websphere.component.j2c;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryInfo;
import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryMBeanLocator;

import com.ibm.websphere.management.exception.ConnectorException;

public class J2CConnectionFactoryMBeanLocator extends ConnectionFactoryMBeanLocator {
    public J2CConnectionFactoryMBeanLocator(String jndiName) {
        super(jndiName);
    }

    @Override
    protected ConnectionFactoryInfo getConnectionFactoryInfo(ManagedServer server, String jndiName) throws JMException, ConnectorException {
        return server.queryConfig(new ConnectionFactoryQuery(server.getNode(), server.getServer())).getByJndiName(jndiName);
    }

    @Override
    protected String getType() {
        // TODO Auto-generated method stub
        return "J2CConnectionFactory";
    }

    @Override
    protected String getProviderKeyProperty() {
        return "J2CResourceAdapter";
    }
}
