package be.fgov.kszbcss.websphere.rhq;

import java.util.Arrays;
import java.util.Collections;

import javax.management.j2ee.statistics.Stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;

public class StatsHelper {
    private static final Log log = LogFactory.getLog(StatsHelper.class);
    
    public static Stats getStats(EmsBean bean) {
        if (bean == null) {
            throw new IllegalArgumentException("getWSStats: bean can't be null");
        }
        EmsAttribute statsAttribute = bean.refreshAttributes(Collections.singletonList("stats")).get(0);
        Stats stats = (Stats)statsAttribute.getValue();
        if (log.isDebugEnabled()) {
            if (stats == null) {
                log.debug("No stats attribute found on " + bean.getBeanName());
            } else {
                log.debug("Loaded statistics from MBean " + bean.getBeanName()
                        + ":\n  Stats type: " + stats.getClass().getName()
                        + "\n  Available statistics: " + Arrays.asList(stats.getStatisticNames()));
            }
        }
        return stats;
    }
}
