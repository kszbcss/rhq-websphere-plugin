package be.fgov.kszbcss.rhq.websphere.component;

import java.util.Map;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.mbean.DynamicMBeanObjectNamePatternLocator;
import be.fgov.kszbcss.rhq.websphere.mbean.ProcessInfo;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public final class ConnectionFactoryMBeanLocator extends DynamicMBeanObjectNamePatternLocator {
    private final ConnectionFactoryType type;
    private final String jndiName;

    public ConnectionFactoryMBeanLocator(ConnectionFactoryType type, String jndiName) {
        super("WebSphere", false);
        this.type = type;
        this.jndiName = jndiName;
    }

    @Override
    protected void applyKeyProperties(ProcessInfo processInfo, AdminClient adminClient, Map<String,String> props) throws JMException, ConnectorException {
        ManagedServer server = (ManagedServer)processInfo.getServer();
        ConnectionFactoryInfo cf = server.queryConfig(new ConnectionFactoryQuery(server.getNode(), server.getServer(), type)).getByJndiName(jndiName);
        if (cf == null) {
            throw new JMException("A " + type.getConfigurationObjectType() + " with JNDI name " + jndiName + " doesn't exist in the configuration");
        }
        props.put("type", type.getConfigurationObjectType());
        props.put("name", cf.getName());
        props.put(type.getProviderKeyProperty(), cf.getProviderName());
    }
    
    // TODO: implement toString, equals and hashCode

}
