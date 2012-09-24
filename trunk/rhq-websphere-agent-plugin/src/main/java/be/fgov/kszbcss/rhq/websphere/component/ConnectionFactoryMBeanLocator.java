package be.fgov.kszbcss.rhq.websphere.component;

import java.util.Map;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.mbean.DynamicMBeanObjectNamePatternLocator;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * MBean locator implementation for connection factories. This locator uses the MBean identifier
 * derived from the configuration data to locate the corresponding MBean. The reason is that
 * WebSphere doesn't enforce the uniqueness of the (scope, provider name, data source name) triplet
 */
public final class ConnectionFactoryMBeanLocator extends DynamicMBeanObjectNamePatternLocator {
    private final ConnectionFactoryType type;
    private final String jndiName;

    public ConnectionFactoryMBeanLocator(ConnectionFactoryType type, String jndiName) {
        super("WebSphere", false);
        this.type = type;
        this.jndiName = jndiName;
    }

    @Override
    protected void applyKeyProperties(WebSphereServer server, Map<String,String> props) throws JMException, ConnectorException, InterruptedException {
        ConnectionFactoryInfo cf = ((ApplicationServer)server).queryConfig(new ConnectionFactoryQuery(server.getNode(), server.getServer(), type), false).getByJndiName(jndiName);
        if (cf == null) {
            throw new JMException("A " + type.getConfigurationObjectType() + " with JNDI name " + jndiName + " doesn't exist in the configuration");
        }
        props.put("type", type.getConfigurationObjectType());
        props.put("mbeanIdentifier", cf.getId().replace('|', '/'));
    }
    
    // TODO: implement equals and hashCode

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + type + "," + jndiName + ")";
    }
}
