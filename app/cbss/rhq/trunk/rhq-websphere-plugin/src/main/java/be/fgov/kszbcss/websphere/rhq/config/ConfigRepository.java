package be.fgov.kszbcss.websphere.rhq.config;

import com.ibm.websphere.management.repository.ConfigEpoch;

public interface ConfigRepository {
    ConfigEpoch getRepositoryEpoch();
}
