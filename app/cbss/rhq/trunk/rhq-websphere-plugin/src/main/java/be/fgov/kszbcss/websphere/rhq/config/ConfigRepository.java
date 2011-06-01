package be.fgov.kszbcss.websphere.rhq.config;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.management.repository.ConfigEpoch;

/**
 * Proxy interface for the <tt>ConfigRepository</tt> MBean.
 */
public interface ConfigRepository {
    ConfigEpoch getRepositoryEpoch() throws JMException, ConnectorException;
    String[] listResourceNames(String parent, int type, int depth) throws JMException, ConnectorException;
    byte[] extract(String docURI) throws JMException, ConnectorException;
}
