package be.fgov.kszbcss.rhq.websphere.component.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigServiceWrapper;

import com.ibm.websphere.management.exception.ConnectorException;

public class DataSourceQuery implements ConfigQuery<DataSources> {
    private static final long serialVersionUID = 6533346488075959L;
    
    private final String node;
    private final String server;
    
    public DataSourceQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public DataSources execute(ConfigServiceWrapper configService) throws JMException, ConnectorException {
        List<DataSourceInfo> result = new ArrayList<DataSourceInfo>();
        for (ConfigObject dataSource : configService.allScopes(node, server).path("JDBCProvider").path("DataSource").resolve()) {
            Map<String,Object> properties = new HashMap<String,Object>();
            for (ConfigObject resourceProperty : ((ConfigObject)dataSource.getAttribute("propertySet")).getChildren("resourceProperties")) {
                properties.put((String)resourceProperty.getAttribute("name"), resourceProperty.getAttribute("value"));
            }
            ConfigObject provider = (ConfigObject)dataSource.getAttribute("provider");
            // TODO: remove duplicate jndi names!
            result.add(new DataSourceInfo(
                    (String)provider.getAttribute("name"),
                    (String)dataSource.getAttribute("name"),
                    (String)dataSource.getAttribute("jndiName"),
                    properties));
        }
        return new DataSources(result.toArray(new DataSourceInfo[result.size()]));
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
