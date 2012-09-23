package com.ibm.websphere.pmi.stat;

public interface WSStatistic {
    int getId();
    
    String getName();
    
    String getUnit();
    
    String getDescription();
    
    long getStartTime();
    
    long getLastSampleTime();
}
