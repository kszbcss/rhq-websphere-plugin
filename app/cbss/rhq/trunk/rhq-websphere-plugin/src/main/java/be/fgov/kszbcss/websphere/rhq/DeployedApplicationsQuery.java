package be.fgov.kszbcss.websphere.rhq;

import java.util.List;

import javax.management.JMException;
import javax.management.ObjectName;

import be.fgov.kszbcss.websphere.rhq.config.ConfigQuery;
import be.fgov.kszbcss.websphere.rhq.config.ConfigServiceWrapper;

import com.ibm.websphere.management.exception.ConnectorException;

public class DeployedApplicationsQuery implements ConfigQuery<String[]> {
    private static final long serialVersionUID = 3198911266754286723L;
    
    private final String node;
    private final String server;

    public DeployedApplicationsQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public String[] execute(ConfigServiceWrapper configService) throws JMException, ConnectorException {
        // AdminConfig.getid("/Node:twas02/ServerIndex:/ServerEntry:TENVCBSS.AppCluster.twas02.1/")
        // AdminConfig.showAttribute("TENVCBSS.AppCluster.twas02.1(cells/tcell/nodes/twas02|serverindex.xml#ServerEntry_1306410389272)", "deployedApplications")
        
        // TODO: check number of returned results
        ObjectName serverEntry = configService.resolve("Node=" + node + ":ServerIndex=:ServerEntry=" + server)[0];
        List<?> deployedApplications = (List<?>)configService.getAttribute(serverEntry, "deployedApplications");
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
}
