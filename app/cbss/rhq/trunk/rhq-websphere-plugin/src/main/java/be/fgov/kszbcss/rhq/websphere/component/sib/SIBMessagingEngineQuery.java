package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigServiceWrapper;

import com.ibm.websphere.management.exception.ConnectorException;

public class SIBMessagingEngineQuery implements ConfigQuery<SIBMessagingEngineInfo[]> {
    private static final long serialVersionUID = 4836748290502191854L;
    
    private final String node;
    private final String server;
    
    public SIBMessagingEngineQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public SIBMessagingEngineInfo[] execute(ConfigServiceWrapper configService) throws JMException, ConnectorException {
        List<SIBMessagingEngineInfo> result = new ArrayList<SIBMessagingEngineInfo>();
        for (ConfigObject me : configService.allScopes(node, server).path("SIBMessagingEngine").resolve()) {
            result.add(new SIBMessagingEngineInfo((String)me.getAttribute("name"), (String)me.getAttribute("busName")));
        }
        return result.toArray(new SIBMessagingEngineInfo[result.size()]);
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SIBMessagingEngineQuery) {
            SIBMessagingEngineQuery other = (SIBMessagingEngineQuery)obj;
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
