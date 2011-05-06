package com.ibm.websphere.ras;

import java.io.Serializable;

public interface RasMessage extends Serializable {
    String INFO = "Informational";
    
    String AUDIT = "Audit";
    
    String SERVICE = "Service";
    
    String WARNING = "Warning";
    
    String ERROR = "Error";
    
    String FATAL = "Fatal";
    
    String UNKNOWN = "Unknown";
    
    long getTimeStamp();
    
    String getThreadId();
    
    String getMessageSeverity();
    
    String getMessageKey();
    
    String getResourceBundleName();
    
    String getMessageOriginator();
    
    String getLocalizedMessage(java.util.Locale arg0);
}
