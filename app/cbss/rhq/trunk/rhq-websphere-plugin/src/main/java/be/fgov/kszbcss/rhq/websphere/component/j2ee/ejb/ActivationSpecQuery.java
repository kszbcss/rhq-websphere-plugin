package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;

import com.ibm.websphere.management.exception.ConnectorException;

public class ActivationSpecQuery implements ConfigQuery<ActivationSpecs> {
    private static final long serialVersionUID = 8754174264585470653L;
    
    private static final Log log = LogFactory.getLog(ActivationSpecQuery.class);
    
    private final String node;
    private final String server;
    
    public ActivationSpecQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public ActivationSpecs execute(CellConfiguration config) throws JMException, ConnectorException {
        Map<String,ActivationSpecInfo> map = new HashMap<String,ActivationSpecInfo>();
        for (ConfigObject ra : config.allScopes(node, server).path("J2CResourceAdapter", "SIB JMS Resource Adapter").resolve()) {
            for (ConfigObject activationSpec : ra.getChildren("j2cActivationSpec")) {
                String jndiName = (String)activationSpec.getAttribute("jndiName");
                if (!map.containsKey(jndiName)) {
                    map.put(jndiName, new ActivationSpecInfo((String)activationSpec.getAttribute("destinationJndiName")));
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Loaded activation specs for node '" + node + "' and server '" + server + "': " + map);
        }
        return new ActivationSpecs(map);
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActivationSpecQuery) {
            ActivationSpecQuery other = (ActivationSpecQuery)obj;
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
