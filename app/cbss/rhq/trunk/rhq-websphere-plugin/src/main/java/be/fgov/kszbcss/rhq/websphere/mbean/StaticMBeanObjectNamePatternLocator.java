package be.fgov.kszbcss.rhq.websphere.mbean;

import javax.management.JMException;
import javax.management.ObjectName;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on an object name pattern. This implementation assumes that the pattern is
 * known when the locator is constructed.
 */
public class StaticMBeanObjectNamePatternLocator extends MBeanObjectNamePatternLocator {
    private final ObjectName pattern;
    
    public StaticMBeanObjectNamePatternLocator(ObjectName pattern, boolean recursive) {
        super(recursive);
        this.pattern = pattern;
    }

    public StaticMBeanObjectNamePatternLocator(ObjectName pattern) {
        this(pattern, false);
    }
    
    @Override
    protected ObjectName getPattern(WebSphereServer server) throws JMException, ConnectorException {
        return pattern;
    }

    @Override
    public String toString() {
        return pattern.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StaticMBeanObjectNamePatternLocator
                && pattern.equals(((StaticMBeanObjectNamePatternLocator)obj).pattern);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }
}
