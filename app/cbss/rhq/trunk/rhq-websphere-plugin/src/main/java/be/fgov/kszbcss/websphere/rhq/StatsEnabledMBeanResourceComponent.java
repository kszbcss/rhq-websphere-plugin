package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.plugins.jmx.JMXComponent;
import org.rhq.plugins.jmx.MBeanResourceComponent;

public class StatsEnabledMBeanResourceComponent<T extends JMXComponent> extends MBeanResourceComponent<T> {
    private final Log log = LogFactory.getLog(StatsEnabledMBeanResourceComponent.class);
    
    @Override
    protected void getValues(MeasurementReport report, Set requests, EmsBean bean) {
        Set<MeasurementScheduleRequest> simpleRequests = new HashSet<MeasurementScheduleRequest>();
        // We create a new PropertyUtilsBean every time in order to avoid keeping references
        // to class loaders used by EMS (which could create a leak).
        PropertyUtilsBean propUtils = new PropertyUtilsBean();
        WSStatsWrapper wsStats = null;
        for (MeasurementScheduleRequest request : (Set<MeasurementScheduleRequest>)requests) {
            String name = request.getName();
            if (name.startsWith("stats.")) {
                if (wsStats == null) {
                    wsStats = getWSStats(new StatsHelper(propUtils));
                }
                if (wsStats != null) {
                    int idx = name.lastIndexOf('.');
                    String statisticName = name.substring(6, idx);
                    String propertyName = name.substring(idx+1);
                    Object statistic;
                    try {
                        statistic = wsStats.getStatistic(statisticName);
                    } catch (Exception ex) {
                        log.error("Unable to retrieve statistic with name " + statisticName, ex);
                        continue;
                    }
                    if (statistic == null) {
                        log.error("Statistic with name " + statisticName + " not available");
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Loaded Statistic with name " + statisticName + " and type " + statistic.getClass().getName());
                    }
                    Number value;
                    try {
                        value = (Number)propUtils.getProperty(statistic, propertyName);
                    } catch (Exception ex) {
                        log.error("Failed to get the " + propertyName + " from the Statistic object for " + statisticName, ex);
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Adding measurement for " + name + "; value=" + value);
                    }
                    report.addData(new MeasurementDataNumeric(request, value.doubleValue()));
                }
            } else {
                simpleRequests.add(request);
            }
        }
        if (!simpleRequests.isEmpty()) {
            super.getValues(report, simpleRequests, bean);
        }
    }

    protected WSStatsWrapper getWSStats(StatsHelper statsHelper) {
        return statsHelper.getWSStats(getEmsBean());
    }
}
