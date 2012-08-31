package com.ibm.websphere.pmi.stat;

public interface WSStats {
    String getName();
    
    String getStatsType();
    
    long getTime();
    
    WSStatistic getStatistic(int dataId);
    
    WSStatistic getStatistic(String name);
    
    String[] getStatisticNames();
    
    WSStatistic[] getStatistics();
    
    WSStats getStats(String arg0);
    
    WSStats[] getSubStats();
    
    int numStatistics();
}
