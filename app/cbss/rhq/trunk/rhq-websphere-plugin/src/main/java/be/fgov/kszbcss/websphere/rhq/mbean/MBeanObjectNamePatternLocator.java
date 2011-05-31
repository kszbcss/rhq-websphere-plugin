package be.fgov.kszbcss.websphere.rhq.mbean;

import java.util.Set;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on an object name pattern.
 */
public class MBeanObjectNamePatternLocator implements MBeanLocator {
    private static final Log log = LogFactory.getLog(MBeanObjectNamePatternLocator.class.getName());
    
    private final ObjectName pattern;
    private final boolean recursive;
    
    public MBeanObjectNamePatternLocator(ObjectName pattern, boolean recursive) {
        this.pattern = pattern;
        this.recursive = recursive;
    }

    public MBeanObjectNamePatternLocator(ObjectName pattern) {
        this(pattern, false);
    }
    
    public Set<ObjectName> queryNames(ProcessInfo processInfo, AdminClient adminClient) throws JMException, ConnectorException {
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

    @Override
    public String toString() {
        return pattern.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MBeanObjectNamePatternLocator
                && pattern.equals(((MBeanObjectNamePatternLocator)obj).pattern);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }
}
