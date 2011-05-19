package be.fgov.kszbcss.websphere.rhq.mbean;

import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.ObjectName;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on an object name pattern.
 */
public class MBeanObjectNamePatternLocator implements MBeanLocator {
    private final ObjectName pattern;
    
    public MBeanObjectNamePatternLocator(ObjectName pattern) {
        this.pattern = pattern;
    }

    public ObjectName locate(AdminClient adminClient) throws JMException, ConnectorException {
        Set<ObjectName> objectNames = adminClient.queryNames(pattern, null);
        if (objectNames.size() == 0) {
            throw new InstanceNotFoundException("No MBean found for pattern " + pattern);
        } else if (objectNames.size() > 1) {
            throw new InstanceNotFoundException("Multiple MBeans found for pattern " + pattern);
        } else {
            return objectNames.iterator().next();
        }
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
