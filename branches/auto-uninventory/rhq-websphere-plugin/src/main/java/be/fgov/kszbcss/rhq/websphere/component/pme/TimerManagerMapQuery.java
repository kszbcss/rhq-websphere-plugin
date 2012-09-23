package be.fgov.kszbcss.rhq.websphere.component.pme;

import java.util.HashMap;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;

import com.ibm.websphere.management.exception.ConnectorException;

public class TimerManagerMapQuery implements ConfigQuery<HashMap<String,String>> {
    private static final long serialVersionUID = -2899860553017447788L;

    private static final Log log = LogFactory.getLog(TimerManagerMapQuery.class);
    
    private final String node;
    private final String server;
    
    public TimerManagerMapQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public HashMap<String,String> execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException {
        HashMap<String,String> map = new HashMap<String,String>();
        for (ConfigObject wm : config.allScopes(node, server).path("TimerManagerProvider").path("TimerManagerInfo").resolve()) {
            String jndiName = (String)wm.getAttribute("jndiName");
            if (!map.containsKey(jndiName)) {
                map.put(jndiName, (String)wm.getAttribute("name"));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Loaded timer managers for node '" + node + "' and server '" + server + "': " + map);
        }
        return map;
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimerManagerMapQuery) {
            TimerManagerMapQuery other = (TimerManagerMapQuery)obj;
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
