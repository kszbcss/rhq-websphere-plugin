package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;

import com.ibm.websphere.management.exception.ConnectorException;

public class SIBDestinationMapQuery implements ConfigQuery<SIBDestinationMap> {
    private static final long serialVersionUID = 1542229435338345886L;
    
    private static final Log log = LogFactory.getLog(SIBDestinationMapQuery.class);
    
    private final String node;
    private final String server;
    
    public SIBDestinationMapQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public SIBDestinationMap execute(CellConfiguration config) throws JMException, ConnectorException {
        Map<String,SIBDestination> map = new HashMap<String,SIBDestination>();
        for (ConfigObject ra : config.allScopes(node, server).path("J2CResourceAdapter", "SIB JMS Resource Adapter").resolve()) {
            for (ConfigObject adminObject : ra.getChildren("j2cAdminObjects")) {
                String jndiName = (String)adminObject.getAttribute("jndiName");
                if (!map.containsKey(jndiName)) {
                    String busName = null;
                    String destinationName = null;
                    for (ConfigObject property : adminObject.getChildren("properties")) {
                        String propName = (String)property.getAttribute("name");
                        if (propName.equals("BusName")) {
                            busName = (String)property.getAttribute("value");
                        } else if (propName.equals("QueueName")) {
                            destinationName = (String)property.getAttribute("value");
                        }
                    }
                    map.put(jndiName, new SIBDestination(busName, destinationName));
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Loaded SIB destinations for node '" + node + "' and server '" + server + "': " + map);
        }
        return new SIBDestinationMap(map);
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SIBDestinationMapQuery) {
            SIBDestinationMapQuery other = (SIBDestinationMapQuery)obj;
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
