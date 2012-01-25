package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.List;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;

import com.ibm.websphere.management.exception.ConnectorException;

public class DeployedApplicationsQuery implements ConfigQuery<String[]> {
    private static final long serialVersionUID = 3198911266754286723L;
    
    private final String node;
    private final String server;

    public DeployedApplicationsQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public String[] execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException {
        // AdminConfig.getid("/Node:twas02/ServerIndex:/ServerEntry:TENVCBSS.AppCluster.twas02.1/")
        // AdminConfig.showAttribute("TENVCBSS.AppCluster.twas02.1(cells/tcell/nodes/twas02|serverindex.xml#ServerEntry_1306410389272)", "deployedApplications")
        
        ConfigObject serverEntry = config.node(node).path("ServerIndex").path("ServerEntry", server).resolveSingle();
        List<?> deployedApplications = (List<?>)serverEntry.getAttribute("deployedApplications");
        String[] applicationNames = new String[deployedApplications.size()];
        int i = 0;
        for (Object deployedApplication : deployedApplications) {
            String deployment = (String)deployedApplication;
            applicationNames[i++] = deployment.substring(deployment.lastIndexOf('/') + 1);
        }
        return applicationNames;
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeployedApplicationsQuery) {
            DeployedApplicationsQuery other = (DeployedApplicationsQuery)obj;
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
