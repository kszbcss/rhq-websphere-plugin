package be.fgov.kszbcss.websphere.rhq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.plugins.jmx.MBeanResourceComponent;

import com.ibm.websphere.pmi.stat.WSAverageStatistic;
import com.ibm.websphere.pmi.stat.WSCountStatistic;
import com.ibm.websphere.pmi.stat.WSRangeStatistic;
import com.ibm.websphere.pmi.stat.WSStatistic;
import com.ibm.websphere.pmi.stat.WSStats;

public class StatsEnabledMBeanResourceComponent<T extends WebSphereComponent<?>> extends MBeanResourceComponent<T> implements WebSphereComponent<T> {
    private final Log log = LogFactory.getLog(StatsEnabledMBeanResourceComponent.class);
    
    private final Map<String,WSAverageStatistic> lastStats = new HashMap<String,WSAverageStatistic>();
    
    public WebSphereServer getServer() {
        return getResourceContext().getParentResourceComponent().getServer();
    }

    @Override
    protected void getValues(MeasurementReport report, Set requests, EmsBean bean) {
        Set<MeasurementScheduleRequest> simpleRequests = new HashSet<MeasurementScheduleRequest>();
        WSStats stats = null;
        for (MeasurementScheduleRequest request : (Set<MeasurementScheduleRequest>)requests) {
            String name = request.getName();
            if (name.startsWith("stats.")) {
                if (stats == null) {
                    stats = getStats();
                }
                if (stats != null) {
                    String statisticName = name.substring(6);
                    WSStatistic statistic = stats.getStatistic(statisticName);
                    if (statistic == null) {
                        log.error("Statistic with name " + statisticName + " not available");
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Loaded Statistic with name " + statisticName + " and type " + statistic.getClass().getName());
                    }
                    double value;
                    if (statistic instanceof WSCountStatistic) {
                        value = ((WSCountStatistic)statistic).getCount();
                    } else if (statistic instanceof WSRangeStatistic) {
                        value = ((WSRangeStatistic)statistic).getCurrent();
                    } else if (statistic instanceof WSAverageStatistic) {
                        WSAverageStatistic currentStatistic = (WSAverageStatistic)statistic;
                        WSAverageStatistic prevStatistic = lastStats.get(statisticName);
                        lastStats.put(statisticName, currentStatistic);
                        if (prevStatistic == null) {
                            continue;
                        } else {
                            long countDelta = currentStatistic.getCount()-prevStatistic.getCount();
                            if (countDelta > 0) {
                                value = ((double)(currentStatistic.getTotal()-prevStatistic.getTotal())) / ((double)countDelta);
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

    protected WSStats getStats() {
        return getServer().getWSStats(getEmsBean());
    }
}
