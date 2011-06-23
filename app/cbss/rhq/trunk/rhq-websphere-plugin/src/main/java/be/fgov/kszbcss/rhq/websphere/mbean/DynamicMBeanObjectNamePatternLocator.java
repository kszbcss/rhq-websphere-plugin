package be.fgov.kszbcss.rhq.websphere.mbean;

import java.util.Hashtable;
import java.util.Map;

import javax.management.JMException;
import javax.management.ObjectName;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on an object name pattern constructed dynamically. This implementation
 * should be used when the pattern is not known at construction time and can only be determined
 * later.
 */
public abstract class DynamicMBeanObjectNamePatternLocator extends MBeanObjectNamePatternLocator {
    private final String domain;
    
    public DynamicMBeanObjectNamePatternLocator(String domain, boolean recursive) {
        super(recursive);
        this.domain = domain;
    }

    @Override
    protected final ObjectName getPattern(ProcessInfo processInfo, AdminClient adminClient) throws JMException, ConnectorException {
        Hashtable<String,String> props = new Hashtable<String,String>();
        applyKeyProperties(processInfo, adminClient, props);
        return new ObjectName(new ObjectName(domain, props).toString() + ",*");
    }
    
    protected abstract void applyKeyProperties(ProcessInfo processInfo, AdminClient adminClient, Map<String,String> props) throws JMException, ConnectorException;
}