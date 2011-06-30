package be.fgov.kszbcss.rhq.websphere.component.j2c;

import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigServiceWrapper;

import com.ibm.websphere.management.exception.ConnectorException;

public class ConnectionFactoryQuery implements ConfigQuery<ConnectionFactories> {
    private static final long serialVersionUID = -8161418420026623081L;
    
    private final String node;
    private final String server;
    
    public ConnectionFactoryQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }
    
    public ConnectionFactories execute(ConfigServiceWrapper configService) throws JMException, ConnectorException {
        List<ConnectionFactoryInfo> result = new ArrayList<ConnectionFactoryInfo>();
        for (ConfigObject dataSource : configService.allScopes(node, server).path("J2CResourceAdapter").path("J2CConnectionFactory").resolve()) {
            String jndiName = (String)dataSource.getAttribute("jndiName");
            // If no JNDI name is defined, then it's probably a J2CConnectionFactory corresponding to a JDBC data source
            if (jndiName != null) {
                ConfigObject provider = (ConfigObject)dataSource.getAttribute("provider");
                // TODO: remove duplicate jndi names!
                result.add(new ConnectionFactoryInfo(
                        (String)provider.getAttribute("name"),
                        (String)dataSource.getAttribute("name"),
                        jndiName));
            }
        }
        return new ConnectionFactories(result.toArray(new ConnectionFactoryInfo[result.size()]));
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnectionFactoryQuery) {
            ConnectionFactoryQuery other = (ConnectionFactoryQuery)obj;
            return other.node.equals(node) && other.server.equals(server);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + node + "," + server + ")";
    }
}
