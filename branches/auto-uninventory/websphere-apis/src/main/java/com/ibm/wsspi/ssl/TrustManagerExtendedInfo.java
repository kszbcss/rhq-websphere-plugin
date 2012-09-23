package com.ibm.wsspi.ssl;

import java.util.Map;
import java.util.Properties;

public interface TrustManagerExtendedInfo {
    void setCustomProperties(Properties customProperties);
    
    void setExtendedInfo(Map info);
    
    void setSSLConfig(Properties config);
}
