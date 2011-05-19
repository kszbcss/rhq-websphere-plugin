package be.fgov.kszbcss.websphere.rhq.mbean;

import javax.management.JMException;
import javax.management.ObjectName;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on some criteria. Instances of this class are used as cache keys.
 * Therefore implementations must override {@link Object#equals(Object)} and
 * {@link Object#hashCode()}.
 */
public interface MBeanLocator {
    ObjectName locate(AdminClient adminClient) throws JMException, ConnectorException;
}
