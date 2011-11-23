package be.fgov.kszbcss.rhq.websphere.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;

import com.ibm.websphere.management.exception.ConnectorException;

public class ConnectionFactoryQuery implements ConfigQuery<ConnectionFactories> {
    private static final long serialVersionUID = 6533346488075959L;
    
    private final String node;
    private final String server;
    private final ConnectionFactoryType type;
    
    public ConnectionFactoryQuery(String node, String server, ConnectionFactoryType type) {
        this.node = node;
        this.server = server;
        this.type = type;
    }

    public ConnectionFactories execute(CellConfiguration config) throws JMException, ConnectorException {
        List<ConnectionFactoryInfo> result = new ArrayList<ConnectionFactoryInfo>();
        for (ConfigObject cf : config.allScopes(node, server).path(type.getContainingConfigurationObjectType()).path(type.getConfigurationObjectType()).resolve()) {
            String jndiName = (String)cf.getAttribute("jndiName");
            // If no JNDI name is defined, then it's probably a J2CConnectionFactory corresponding to a JDBC data source
            if (jndiName != null) {
                Map<String,Object> properties = new HashMap<String,Object>();
                for (ConfigObject resourceProperty : ((ConfigObject)cf.getAttribute("propertySet")).getChildren("resourceProperties")) {
                    properties.put((String)resourceProperty.getAttribute("name"), resourceProperty.getAttribute("value"));
                }
                ConfigObject provider = (ConfigObject)cf.getAttribute("provider");
                // TODO: remove duplicate jndi names!
                result.add(new ConnectionFactoryInfo(
                        cf.getId(),
                        (String)provider.getAttribute("name"),
                        (String)cf.getAttribute("name"),
                        jndiName,
                        properties));
            }
        }
        return new ConnectionFactories(result.toArray(new ConnectionFactoryInfo[result.size()]));
    }

    @Override
    public int hashCode() {
        return 31*31*node.hashCode() + 31*server.hashCode() + type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnectionFactoryQuery) {
            ConnectionFactoryQuery other = (ConnectionFactoryQuery)obj;
            return other.node.equals(node) && other.server.equals(server) && other.type.equals(type);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + node + "," + server + "," + type + ")";
    }
}
