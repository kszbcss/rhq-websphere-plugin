package be.fgov.kszbcss.rhq.websphere.component;

import java.util.Map;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.j2c.ConnectionFactoryQuery;
import be.fgov.kszbcss.rhq.websphere.component.j2c.J2CConnectionFactoryInfo;
import be.fgov.kszbcss.rhq.websphere.mbean.DynamicMBeanObjectNamePatternLocator;
import be.fgov.kszbcss.rhq.websphere.mbean.ProcessInfo;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public abstract class ConnectionFactoryMBeanLocator extends DynamicMBeanObjectNamePatternLocator {
    private final String jndiName;

    public ConnectionFactoryMBeanLocator(String jndiName) {
        super("WebSphere", false);
        this.jndiName = jndiName;
    }

    @Override
    protected void applyKeyProperties(ProcessInfo processInfo, AdminClient adminClient, Map<String,String> props) throws JMException, ConnectorException {
        ManagedServer server = (ManagedServer)processInfo.getServer();
        ConnectionFactoryInfo cf = getConnectionFactoryInfo(server, jndiName);
        if (cf == null) {
            throw new JMException("A " + getType() + " with JNDI name " + jndiName + " doesn't exist in the configuration");
        }
        props.put("type", getType());
        props.put("name", cf.getName());
        props.put(getProviderKeyProperty(), cf.getProviderName());
    }
    
    protected abstract ConnectionFactoryInfo getConnectionFactoryInfo(ManagedServer server, String jndiName) throws JMException, ConnectorException;
    protected abstract String getType();
    protected abstract String getProviderKeyProperty();
    
    // TODO: implement toString, equals and hashCode

}
