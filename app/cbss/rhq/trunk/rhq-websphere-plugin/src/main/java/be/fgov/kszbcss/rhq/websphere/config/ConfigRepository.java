package be.fgov.kszbcss.rhq.websphere.config;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.management.repository.ConfigEpoch;
import com.ibm.websphere.management.repository.DocumentContentSource;

/**
 * Proxy interface for the <tt>ConfigRepository</tt> MBean.
 */
public interface ConfigRepository {
    ConfigEpoch getRepositoryEpoch() throws JMException, ConnectorException;
    String[] listResourceNames(String parent, int type, int depth) throws JMException, ConnectorException;
    DocumentContentSource extract(String docURI) throws JMException, ConnectorException;
}
