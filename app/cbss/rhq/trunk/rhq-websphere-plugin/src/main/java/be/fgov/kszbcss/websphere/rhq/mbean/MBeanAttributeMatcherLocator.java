package be.fgov.kszbcss.websphere.rhq.mbean;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.ObjectName;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on an object name pattern and an attribute value to match.
 */
public class MBeanAttributeMatcherLocator implements MBeanLocator {
    private final ObjectName pattern;
    private final String attributeName;
    private final String attributeValue;
    
    public MBeanAttributeMatcherLocator(ObjectName pattern,
            String attributeName, String attributeValue) {
        this.pattern = pattern;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public ObjectName locate(AdminClient adminClient) throws JMException, ConnectorException {
        for (ObjectName objectName : adminClient.queryNames(pattern, null)) {
            if (adminClient.getAttribute(objectName, attributeName).equals(attributeValue)) {
                return objectName;
            }
        }
        throw new InstanceNotFoundException("No MBean instance found for pattern " + pattern + " and " + attributeName + "=" + attributeValue);
    }

    @Override
    public String toString() {
        return pattern + "[" + attributeName + "=" + attributeValue + "]";
    }
}
