package com.ibm.websphere.pmi.stat;

public interface WSRangeStatistic extends WSStatistic {
    long getHighWaterMark();
    
    long getLowWaterMark();
    
    long getCurrent();
    
    double getIntegral();
    
    double getMean();
}
