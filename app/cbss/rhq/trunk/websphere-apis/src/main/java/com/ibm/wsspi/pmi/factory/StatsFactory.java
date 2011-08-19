package com.ibm.wsspi.pmi.factory;

import javax.management.ObjectName;

public class StatsFactory {
    public static StatsGroup createStatsGroup(String groupName, String statsTemplate, ObjectName mBean) throws StatsFactoryException {
        return null;
    }
    
    public static StatsInstance createStatsInstance(String instanceName, StatsGroup parentGroup, ObjectName mBean, StatisticActions listener) throws StatsFactoryException {
        return null;
    }
    
    public static StatsInstance createStatsInstance(String instanceName, String statsTemplate, ObjectName mBean, StatisticActions listener) throws StatsFactoryException {
        return null;
    }
    
    public static void removeStatsGroup(StatsGroup group) throws StatsFactoryException {
        
    }
    
    public static void removeStatsInstance(StatsInstance instance) throws StatsFactoryException {
        
    }
}
