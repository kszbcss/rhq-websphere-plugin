package be.fgov.kszbcss.rhq.websphere.component.server;

import java.util.List;

import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.ObjectName;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigServiceWrapper;

import com.ibm.websphere.management.configservice.ConfigServiceHelper;
import com.ibm.websphere.management.configservice.SystemAttributes;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Query to retrieve the <tt>ThreadPool</tt> configuration objects from the
 * <tt>ThreadPoolManager</tt>.
 */
public class ThreadPoolQuery implements ConfigQuery<ThreadPoolConfiguration[]> {
    private static final long serialVersionUID = -7203291446803851440L;
    
    private final String node;
    private final String server;

    public ThreadPoolQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public ThreadPoolConfiguration[] execute(ConfigServiceWrapper configService) throws JMException, ConnectorException {
        ObjectName threadPoolManager = configService.server(node, server).path("ThreadPoolManager").resolveSingle();
        List<AttributeList> threadPools = (List<AttributeList>)configService.getAttribute(threadPoolManager, "threadPools");
        ThreadPoolConfiguration[] configs = new ThreadPoolConfiguration[threadPools.size()];
        int i = 0;
        for (AttributeList threadPool : threadPools) {
            configs[i++] = new ThreadPoolConfiguration((String)ConfigServiceHelper.getAttributeValue(threadPool, "name") /*,
                    (String)ConfigServiceHelper.getAttributeValue(threadPool, SystemAttributes._WEBSPHERE_CONFIG_DATA_ID) */);
        }
        return configs;
    }


    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ThreadPoolQuery) {
            ThreadPoolQuery other = (ThreadPoolQuery)obj;
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