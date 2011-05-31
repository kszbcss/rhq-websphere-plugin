package be.fgov.kszbcss.websphere.rhq.mbean;

import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;
import javax.management.ObjectName;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on an object name pattern and an attribute value to match.
 */
public class MBeanAttributeMatcherLocator implements MBeanLocator {
    private final MBeanLocator parent;
    private final String attributeName;
    private final String attributeValue;
    
    public MBeanAttributeMatcherLocator(MBeanLocator parent,
            String attributeName, String attributeValue) {
        this.parent = parent;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public MBeanAttributeMatcherLocator(ObjectName pattern,
            String attributeName, String attributeValue) {
        this(new MBeanObjectNamePatternLocator(pattern), attributeName, attributeValue);
    }

    public Set<ObjectName> queryNames(ProcessInfo processInfo, AdminClient adminClient) throws JMException, ConnectorException {
        Set<ObjectName> result = new HashSet<ObjectName>();
        for (ObjectName objectName : parent.queryNames(processInfo, adminClient)) {
            if (adminClient.getAttribute(objectName, attributeName).equals(attributeValue)) {
                result.add(objectName);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return parent + "[" + attributeName + "=" + attributeValue + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MBeanAttributeMatcherLocator) {
            MBeanAttributeMatcherLocator other = (MBeanAttributeMatcherLocator)obj;
            return parent.equals(other.parent) && attributeName.equals(other.attributeName) && attributeValue.equals(other.attributeValue);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31*result + attributeName.hashCode();
        result = 31*result + attributeValue.hashCode();
        return result;
    }
}
