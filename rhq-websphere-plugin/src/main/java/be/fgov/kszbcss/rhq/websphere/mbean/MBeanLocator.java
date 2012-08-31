package be.fgov.kszbcss.rhq.websphere.mbean;

import java.util.Set;

import javax.management.JMException;
import javax.management.ObjectName;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on some criteria. Instances of this class are used as cache keys.
 * Therefore implementations must override {@link Object#equals(Object)} and
 * {@link Object#hashCode()}.
 */
public interface MBeanLocator {
    Set<ObjectName> queryNames(WebSphereServer server) throws JMException, ConnectorException, InterruptedException;
}
