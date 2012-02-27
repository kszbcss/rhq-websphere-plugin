package be.fgov.kszbcss.rhq.websphere.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;

import com.ibm.websphere.management.exception.ConnectorException;

public class ConnectionFactoryQuery implements ConfigQuery<ConnectionFactories> {
    private static final long serialVersionUID = 6533346488075959L;
    
    private static final Log log = LogFactory.getLog(ConnectionFactoryQuery.class);
    
    private final String node;
    private final String server;
    private final ConnectionFactoryType type;
    
    public ConnectionFactoryQuery(String node, String server, ConnectionFactoryType type) {
        this.node = node;
        this.server = server;
        this.type = type;
    }

    public ConnectionFactories execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException {
        List<ConnectionFactoryInfo> result = new ArrayList<ConnectionFactoryInfo>();
        for (ConfigObject cf : config.allScopes(node, server).path(type.getContainingConfigurationObjectType()).path(type.getConfigurationObjectType()).resolve()) {
            String jndiName = (String)cf.getAttribute("jndiName");
            // If no JNDI name is defined, then it's probably a J2CConnectionFactory corresponding to a JDBC data source
            if (jndiName != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving properties for " + jndiName);
                }
                Map<String,Object> properties = new HashMap<String,Object>();
                for (ConfigObject resourceProperty : ((ConfigObject)cf.getAttribute("propertySet")).getChildren("resourceProperties")) {
                    String name = (String)resourceProperty.getAttribute("name");
                    String stringValue = (String)resourceProperty.getAttribute("value");
                    String type = (String)resourceProperty.getAttribute("type");
                    Object value;
                    // TODO: add support for other types
                    if (stringValue == null || stringValue.length() == 0 && !type.equals("java.lang.String")) {
                        value = null;
                    } else if (type == null) {
                        value = stringValue;
                    } else if (type.equals("java.lang.Integer")) {
                        value = Integer.valueOf(stringValue);
                    } else {
                        value = stringValue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("name=" + name + ", type=" + type + ", stringValue=" + stringValue
                                + ", value=" + value + ", class=" + (value == null ? "<N/A>" : value.getClass().getName()));
                    }
                    properties.put(name, value);
                }
                ConfigObject provider = (ConfigObject)cf.getAttribute("provider");
                // TODO: remove duplicate jndi names!
                result.add(new ConnectionFactoryInfo(
                        cf.getId(),
                        (String)provider.getAttribute("name"),
                        (String)cf.getAttribute("name"),
                        jndiName,
                        type == ConnectionFactoryType.JDBC ? (String)cf.getAttribute("datasourceHelperClassname") : null,
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
