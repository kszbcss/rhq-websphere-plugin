package be.fgov.kszbcss.rhq.websphere.config;

import java.io.Serializable;

import com.ibm.websphere.management.repository.ConfigEpoch;

class ConfigQueryResult implements Serializable {
    private static final long serialVersionUID = 1595659913195045702L;
    
    ConfigEpoch epoch;
    Object object;
}
