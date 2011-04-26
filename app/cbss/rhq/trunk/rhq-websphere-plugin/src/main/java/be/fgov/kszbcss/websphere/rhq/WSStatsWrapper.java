package be.fgov.kszbcss.websphere.rhq;

import java.lang.reflect.Method;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WSStatsWrapper {
    private static final Log log = LogFactory.getLog(WSStatsWrapper.class);
    
    private final Object wsStats;
    private final Method getStatisticMethod;
    private final PropertyUtilsBean propUtils;
    
    public WSStatsWrapper(Object wsStats, Method getStatisticMethod, PropertyUtilsBean propUtils) {
        this.wsStats = wsStats;
        this.getStatisticMethod = getStatisticMethod;
        this.propUtils = propUtils;
    }

    public String[] getStatisticNames() {
        try {
            return (String[])propUtils.getProperty(wsStats, "statisticNames");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public Object getStatistic(String name) {
        try {
            return getStatisticMethod.invoke(wsStats, name);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public WSStatsWrapper getNestedStatistic(String property, String name) {
        try {
            Object[] statsArray = (Object[])propUtils.getProperty(wsStats, property);
            for (Object nestedStat : statsArray) {
                if (propUtils.getProperty(nestedStat, "name").equals(name)) {
                    return new WSStatsWrapper(nestedStat, getStatisticMethod, propUtils);
                }
            }
            return null;
        } catch (Exception ex) {
            log.error("Unable to get nested stats object", ex);
            return null;
        }
    }
}
