package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;
import javax.management.ObjectName;

import be.fgov.kszbcss.websphere.rhq.config.ConfigQuery;
import be.fgov.kszbcss.websphere.rhq.config.ConfigServiceWrapper;

import com.ibm.websphere.management.exception.ConnectorException;

public class ClusterNameQuery implements ConfigQuery<String> {
    private static final long serialVersionUID = -5410314356440663605L;
    
    private final String cell;
    private final String node;
    private final String server;

    public ClusterNameQuery(String cell, String node, String server) {
        this.cell = cell;
        this.node = node;
        this.server = server;
    }

    public String execute(ConfigServiceWrapper configService) throws JMException, ConnectorException {
        // TODO: check number of returned results
        ObjectName configObject = configService.resolve("Cell=" + cell + ":Node=" + node + ":Server=" + server)[0];
        return (String)configService.getAttribute(configObject, "clusterName");
    }
    
    @Override
    public int hashCode() {
        return 31*31*cell.hashCode() + 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClusterNameQuery) {
            ClusterNameQuery other = (ClusterNameQuery)obj;
            return other.cell.equals(cell) && other.node.equals(node) && other.server.equals(server);
        } else {
            return false;
        }
    }
}
