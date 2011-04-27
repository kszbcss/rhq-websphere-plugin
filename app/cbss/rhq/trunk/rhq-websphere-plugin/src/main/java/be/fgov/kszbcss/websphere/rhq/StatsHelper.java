package be.fgov.kszbcss.websphere.rhq;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;

public class StatsHelper {
    private static final Log log = LogFactory.getLog(StatsHelper.class);
    
    private final PropertyUtilsBean propUtils;
    
    private Method getStatisticMethod;
    
    public StatsHelper(PropertyUtilsBean propUtils) {
        this.propUtils = propUtils;
    }
    
    public WSStatsWrapper getWSStats(EmsBean bean) {
        if (bean == null) {
            throw new IllegalArgumentException("getWSStats: bean can't be null");
        }
        EmsAttribute statsAttribute = bean.refreshAttributes(Collections.singletonList("stats")).get(0);
        Object stats = statsAttribute.getValue();
        if (stats == null) {
            if (log.isDebugEnabled()) {
                log.debug("No stats attribute found on " + bean.getBeanName());
            }
            return null;
        }
        Object wsStats;
        try {
            wsStats = propUtils.getProperty(stats, "WSImpl");
        } catch (Exception ex) {
            log.error("Unable to get WSStats object", ex);
            return null;
        }
        if (getStatisticMethod == null) {
            try {
                getStatisticMethod = wsStats.getClass().getMethod("getStatistic", String.class);
            } catch (Exception ex) {
                log.error("Unable to reflect the getStatistic method");
                return null;
            }
        }
        WSStatsWrapper wrapper = new WSStatsWrapper(wsStats, getStatisticMethod, propUtils);
        if (log.isDebugEnabled()) {
            log.debug("Loaded statistics from MBean " + bean.getBeanName()
                    + ":\n  Stats type: " + stats.getClass().getName()
                    + "\n  WSStats type: " + wsStats.getClass().getName()
                    + "\n  Available statistics: " + Arrays.asList(wrapper.getStatisticNames()));
        }
        return wrapper;
    }
}
