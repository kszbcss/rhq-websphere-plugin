package be.fgov.kszbcss.rhq.websphere.component.server;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigServiceWrapper;

import com.ibm.websphere.management.exception.ConnectorException;

public class ClusterNameQuery implements ConfigQuery<String> {
    private static final long serialVersionUID = -5410314356440663605L;
    
    private final String node;
    private final String server;

    public ClusterNameQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public String execute(ConfigServiceWrapper configService) throws JMException, ConnectorException {
        return (String)configService.server(node, server).resolveSingle().getAttribute("clusterName");
    }
    
    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClusterNameQuery) {
            ClusterNameQuery other = (ClusterNameQuery)obj;
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
