package be.fgov.kszbcss.websphere.rhq.config;

import com.ibm.websphere.management.repository.ConfigEpoch;

/**
 * Proxy interface for the <tt>ConfigRepository</tt> MBean.
 */
public interface ConfigRepository {
    ConfigEpoch getRepositoryEpoch();
}
