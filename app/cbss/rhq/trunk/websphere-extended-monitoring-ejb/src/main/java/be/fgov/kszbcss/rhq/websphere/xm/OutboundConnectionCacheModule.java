package be.fgov.kszbcss.rhq.websphere.xm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.wsspi.pmi.factory.StatisticActions;
import com.ibm.wsspi.pmi.stat.SPIBoundedRangeStatistic;
import com.ibm.wsspi.pmi.stat.SPIStatistic;

public class OutboundConnectionCacheModule extends StatisticActions {
    private static final Log log = LogFactory.getLog(OutboundConnectionCacheModule.class);
    
    private static final int CONNECTIONS_IN_USE_ID = 1;
    private static final int POOL_SIZE_ID = 2;
    
    private final Object outboundConnectionCache;
    private final Method connectionsInUseMethod;
    private final Method poolSizeMethod;
    private SPIBoundedRangeStatistic connectionsInUseStatistic;
    private SPIBoundedRangeStatistic poolSizeStatistic;
    
    public OutboundConnectionCacheModule(Class<?> outboundConnectionCacheClass) throws Exception {
        outboundConnectionCache = outboundConnectionCacheClass.getMethod("getInstance").invoke(null);
        connectionsInUseMethod = outboundConnectionCacheClass.getDeclaredMethod("connectionsInUse");
        connectionsInUseMethod.setAccessible(true);
        poolSizeMethod = outboundConnectionCacheClass.getDeclaredMethod("poolSize");
        poolSizeMethod.setAccessible(true);
    }
    
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
                updateStatistic(connectionsInUseStatistic, connectionsInUseMethod);
                break;
            case POOL_SIZE_ID:
                updateStatistic(poolSizeStatistic, poolSizeMethod);
                break;
        }
    }
    
    private void updateStatistic(SPIBoundedRangeStatistic statistic, Method method) {
        try {
            statistic.set((Integer)method.invoke(outboundConnectionCache));
        } catch (IllegalAccessException ex) {
            log.error("Unable to update statistic " + statistic.getName(), ex);
        } catch (InvocationTargetException ex) {
            log.error("Unable to update statistic " + statistic.getName(), ex.getCause());
        }
    }
}
