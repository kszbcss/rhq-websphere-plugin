package be.fgov.kszbcss.rhq.websphere.mbean;

import java.util.Set;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

import com.ibm.websphere.management.exception.ConnectorException;

public abstract class MBeanObjectNamePatternLocator implements MBeanLocator {
    private static final Log log = LogFactory.getLog(MBeanObjectNamePatternLocator.class.getName());
    
    private final boolean recursive;

    public MBeanObjectNamePatternLocator(boolean recursive) {
        this.recursive = recursive;
    }
    
    protected abstract ObjectName getPattern(WebSphereServer server) throws JMException, ConnectorException, InterruptedException;
    
    public final Set<ObjectName> queryNames(WebSphereServer server) throws JMException, ConnectorException, InterruptedException {
        ObjectName pattern = getPattern(server);
        ObjectName actualPattern;
        if (recursive || server.getProcessType().equals("ManagedProcess")) {
            actualPattern = pattern;
        } else {
            actualPattern = new ObjectName(pattern + ",cell=" + server.getCell() + ",node=" + server.getNode() + ",process=" + server.getServer());
        }
        if (log.isDebugEnabled()) {
            log.debug("Query names for pattern " + actualPattern);
        }
        return server.getAdminClient().queryNames(actualPattern, null);
    }

    
}
