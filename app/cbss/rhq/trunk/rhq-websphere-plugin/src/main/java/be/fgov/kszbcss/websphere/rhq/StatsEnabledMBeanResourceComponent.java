package be.fgov.kszbcss.websphere.rhq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.TimeStatistic;

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
    
    private final Map<String,TimeStatistic> lastStats = new HashMap<String,TimeStatistic>();
    
    @Override
    protected void getValues(MeasurementReport report, Set requests, EmsBean bean) {
        Set<MeasurementScheduleRequest> simpleRequests = new HashSet<MeasurementScheduleRequest>();
        Stats stats = null;
        for (MeasurementScheduleRequest request : (Set<MeasurementScheduleRequest>)requests) {
            String name = request.getName();
            if (name.startsWith("stats.")) {
                if (stats == null) {
                    stats = getStats();
                }
                if (stats != null) {
                    String statisticName = name.substring(6);
                    Statistic statistic = stats.getStatistic(statisticName);
                    if (statistic == null) {
                        log.error("Statistic with name " + statisticName + " not available");
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Loaded Statistic with name " + statisticName + " and type " + statistic.getClass().getName());
                    }
                    double value;
                    if (statistic instanceof CountStatistic) {
                        value = ((CountStatistic)statistic).getCount();
                    } else if (statistic instanceof RangeStatistic) {
                        value = ((RangeStatistic)statistic).getCurrent();
                    } else if (statistic instanceof TimeStatistic) {
                        TimeStatistic timeStatistic = (TimeStatistic)statistic;
                        TimeStatistic prevStatistic = lastStats.get(statisticName);
                        lastStats.put(statisticName, timeStatistic);
                        if (prevStatistic == null) {
                            continue;
                        } else {
                            long countDelta = timeStatistic.getCount()-prevStatistic.getCount();
                            if (countDelta > 0) {
                                value = ((double)(timeStatistic.getTotalTime()-prevStatistic.getTotalTime())) / ((double)countDelta);
                            } else {
                                continue;
                            }
                        }
                    } else {
                        log.error("Unknown or unsupported statistic type " + statistic.getClass().getName());
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Adding measurement for " + name + "; value=" + value);
                    }
                    report.addData(new MeasurementDataNumeric(request, value));
                }
            } else {
                simpleRequests.add(request);
            }
        }
        if (!simpleRequests.isEmpty()) {
            super.getValues(report, simpleRequests, bean);
        }
    }

    protected Stats getStats() {
        return StatsHelper.getStats(getEmsBean());
    }
}
