package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;
import javax.management.ObjectName;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigServiceWrapper;

import com.ibm.websphere.management.exception.ConnectorException;

public class DataSourceQuery implements ConfigQuery<DataSourceInfo[]> {
    private static final long serialVersionUID = -1116319605058770170L;
    
    private final String node;
    private final String server;
    
    public DataSourceQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public DataSourceInfo[] execute(ConfigServiceWrapper configService) throws JMException, ConnectorException {
        List<DataSourceInfo> result = new ArrayList<DataSourceInfo>();
        for (ObjectName dataSource : configService.allScopes(node, server).path("JDBCProvider").path("DataSource").resolve()) {
            ObjectName provider = (ObjectName)configService.getAttribute(dataSource, "provider");
            // TODO: remove duplicate jndi names!
            result.add(new DataSourceInfo(
                    (String)configService.getAttribute(provider, "name"),
                    (String)configService.getAttribute(dataSource, "name"),
                    (String)configService.getAttribute(dataSource, "jndiName")));
        }
        return result.toArray(new DataSourceInfo[result.size()]);
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataSourceQuery) {
            DataSourceQuery other = (DataSourceQuery)obj;
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
