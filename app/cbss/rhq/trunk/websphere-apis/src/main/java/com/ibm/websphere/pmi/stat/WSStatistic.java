package com.ibm.websphere.pmi.stat;

public interface WSStatistic {
    String getName();
    
    String getUnit();
    
    String getDescription();
    
    long getStartTime();
    
    long getLastSampleTime();
}
