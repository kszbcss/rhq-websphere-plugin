package be.fgov.kszbcss.rhq.websphere.component.j2c;

import java.util.Map;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.mbean.DynamicMBeanObjectNamePatternLocator;
import be.fgov.kszbcss.rhq.websphere.mbean.ProcessInfo;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class ConnectionFactoryMBeanLocator extends DynamicMBeanObjectNamePatternLocator {
    private final String jndiName;

    public ConnectionFactoryMBeanLocator(String jndiName) {
        super("WebSphere", false);
        this.jndiName = jndiName;
    }

    @Override
    protected void applyKeyProperties(ProcessInfo processInfo, AdminClient adminClient, Map<String,String> props) throws JMException, ConnectorException {
        ManagedServer server = (ManagedServer)processInfo.getServer();
        ConnectionFactoryInfo cf = server.queryConfig(new ConnectionFactoryQuery(server.getNode(), server.getServer())).getByJndiName(jndiName);
        if (cf == null) {
            throw new JMException("A connection factory with JNDI name " + jndiName + " doesn't exist in the configuration");
        }
        props.put("type", "J2CConnectionFactory");
        props.put("name", cf.getName());
        props.put("J2CResourceAdapter", cf.getProviderName());
    }
    
    // TODO: implement toString, equals and hashCode
}
