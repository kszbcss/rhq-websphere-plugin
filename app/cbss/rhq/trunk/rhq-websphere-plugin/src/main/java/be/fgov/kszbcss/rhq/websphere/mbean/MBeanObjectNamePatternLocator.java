package be.fgov.kszbcss.rhq.websphere.mbean;

import java.util.Set;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public abstract class MBeanObjectNamePatternLocator implements MBeanLocator {
    private static final Log log = LogFactory.getLog(MBeanObjectNamePatternLocator.class.getName());
    
    private final boolean recursive;

    public MBeanObjectNamePatternLocator(boolean recursive) {
        this.recursive = recursive;
    }
    
    protected abstract ObjectName getPattern(ProcessInfo processInfo, AdminClient adminClient) throws JMException, ConnectorException;
    
    public final Set<ObjectName> queryNames(ProcessInfo processInfo, AdminClient adminClient) throws JMException, ConnectorException {
        ObjectName pattern = getPattern(processInfo, adminClient);
        ObjectName actualPattern;
        if (recursive || processInfo.getProcessType().equals("ManagedProcess")) {
            actualPattern = pattern;
        } else {
            actualPattern = new ObjectName(pattern + ",cell=" + processInfo.getCell() + ",node=" + processInfo.getNode() + ",process=" + processInfo.getProcess());
        }
        if (log.isDebugEnabled()) {
            log.debug("Query names for pattern " + actualPattern);
        }
        return adminClient.queryNames(actualPattern, null);
    }

    
}
