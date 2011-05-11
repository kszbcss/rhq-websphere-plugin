package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;
import javax.management.ObjectName;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on some criteria.
 */
public interface MBeanLocator {
    ObjectName locate(AdminClient adminClient) throws JMException, ConnectorException;
}
