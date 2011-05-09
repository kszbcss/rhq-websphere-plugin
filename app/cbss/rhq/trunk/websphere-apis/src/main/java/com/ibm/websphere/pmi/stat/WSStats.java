package com.ibm.websphere.pmi.stat;

public interface WSStats {
    String getName();
    
    String getStatsType();
    
    long getTime();
    
    WSStatistic getStatistic(int arg0);
    
    WSStatistic getStatistic(java.lang.String arg0);
    
    String[] getStatisticNames();
    
    WSStatistic[] getStatistics();
    
    WSStats getStats(String arg0);
    
    WSStats[] getSubStats();
    
    int numStatistics();
}
