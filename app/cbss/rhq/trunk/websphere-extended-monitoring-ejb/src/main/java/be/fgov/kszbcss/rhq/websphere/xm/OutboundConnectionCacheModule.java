package be.fgov.kszbcss.rhq.websphere.xm;

import com.ibm.wsspi.pmi.factory.StatisticActions;
import com.ibm.wsspi.pmi.stat.SPIBoundedRangeStatistic;
import com.ibm.wsspi.pmi.stat.SPIStatistic;

public class OutboundConnectionCacheModule extends StatisticActions {
    private static final int CONNECTIONS_IN_USE_ID = 1;
    private static final int POOL_SIZE_ID = 2;
    
    private SPIBoundedRangeStatistic connectionsInUseStatistic;
    private SPIBoundedRangeStatistic poolSizeStatistic;
    
    @Override
    public void statisticCreated(SPIStatistic statistic) {
        switch (statistic.getId()) {
            case CONNECTIONS_IN_USE_ID:
                connectionsInUseStatistic = (SPIBoundedRangeStatistic)statistic;
                break;
            case POOL_SIZE_ID:
                poolSizeStatistic = (SPIBoundedRangeStatistic)statistic;
                break;
        }
    }

    @Override
    public void updateStatisticOnRequest(int dataId) {
        switch (dataId) {
            case CONNECTIONS_IN_USE_ID:
                
                break;
            case POOL_SIZE_ID:
                
                break;
        }
    }
}
