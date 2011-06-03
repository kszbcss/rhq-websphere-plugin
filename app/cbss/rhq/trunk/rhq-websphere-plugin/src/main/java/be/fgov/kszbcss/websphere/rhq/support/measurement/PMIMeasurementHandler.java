package be.fgov.kszbcss.websphere.rhq.support.measurement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.websphere.rhq.WebSphereServer;
import be.fgov.kszbcss.websphere.rhq.mbean.MBeanClient;

import com.ibm.websphere.pmi.PmiModuleConfig;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.StatDescriptor;
import com.ibm.websphere.pmi.stat.WSAverageStatistic;
import com.ibm.websphere.pmi.stat.WSCountStatistic;
import com.ibm.websphere.pmi.stat.WSRangeStatistic;
import com.ibm.websphere.pmi.stat.WSStatistic;
import com.ibm.websphere.pmi.stat.WSStats;

public class PMIMeasurementHandler implements MeasurementGroupHandler {
    private static final Log log = LogFactory.getLog(PMIMeasurementHandler.class);
    
    private final MBeanClient mbean;
    private final PMIModuleSelector moduleSelector;
    private final Map<String,WSAverageStatistic> lastStats = new HashMap<String,WSAverageStatistic>();
    
    public PMIMeasurementHandler(MBeanClient mbean, String... path) {
        this(mbean, new StaticPMIModuleSelector(path));
    }
    
    public PMIMeasurementHandler(MBeanClient mbean, PMIModuleSelector moduleSelector) {
        this.mbean = mbean;
        this.moduleSelector = moduleSelector;
    }

    public void getValues(WebSphereServer server, MeasurementReport report, Map<String,MeasurementScheduleRequest> requests) {
        ObjectName objectName;
        try {
            objectName = mbean.getObjectName(true); // TODO: we could first try with the cached name
        } catch (Exception ex) {
            log.error("Failed to get object name", ex);
            return;
        }
        String[] path;
        try {
            path = moduleSelector.getPath();
        } catch (Exception ex) {
            log.error("Failed to determine PMI module path", ex);
            return;
        }
        MBeanStatDescriptor descriptor = path.length == 0 ? new MBeanStatDescriptor(objectName) : new MBeanStatDescriptor(objectName, new StatDescriptor(path));
        WSStats stats;
        try {
            stats = server.getWSStats(descriptor);
        } catch (Exception ex) {
            log.error("Failed to get statistics object", ex);
            return;
        }
        if (stats == null) {
            return;
        }
        PmiModuleConfig pmiModuleConfig;
        try {
            pmiModuleConfig = server.getPmiModuleConfig(stats);
        } catch (Exception ex) {
            log.error("Failed to get PMI module configuration", ex);
            return;
        }
        Set<Integer> statisticsToEnable = null;
        
        for (Map.Entry<String,MeasurementScheduleRequest> entry : requests.entrySet()) {
            MeasurementScheduleRequest request = entry.getValue();
            String name = request.getName();
            String statisticName = entry.getKey();
            if (log.isDebugEnabled()) {
                log.debug("Starting to get value for " + name);
            }
            int dataId = pmiModuleConfig.getDataId(statisticName);
            if (dataId == -1) {
                log.error("Could not find statistic with name " + statisticName + " in the PMI module configuration");
                continue;
            }
            // For some WSStats objects, the statistic names don't match the names used by PMI.
            // Therefore we translate all names to data IDs. This also makes it easier to
            // automatically enable the statistics if necessary.
            WSStatistic statistic = stats.getStatistic(dataId);
            if (statistic == null) {
                log.info("Statistic with name " + statisticName + " (ID " + dataId + ") not available; will attempt to enable it");
                if (statisticsToEnable == null) {
                    statisticsToEnable = new HashSet<Integer>();
                }
                statisticsToEnable.add(dataId);
                continue;
            }
            if (log.isDebugEnabled()) {
                log.debug("Loaded Statistic with name " + statisticName + " (ID " + dataId + ") and type " + statistic.getClass().getName());
            }
            double value;
            if (statistic instanceof WSCountStatistic) {
                value = ((WSCountStatistic)statistic).getCount();
            } else if (statistic instanceof WSRangeStatistic) {
                value = getValue(statisticName, (WSRangeStatistic)statistic);
            } else if (statistic instanceof WSAverageStatistic) {
                WSAverageStatistic currentStatistic = (WSAverageStatistic)statistic;
                WSAverageStatistic prevStatistic = lastStats.get(statisticName);
                lastStats.put(statisticName, currentStatistic);
                // We need to detect a statistic reset in a reliable way. Checking delta(count) > 0 is not sufficient.
                // In fact, delta(count) < 0 implies that the statistic has been reset, but it can happen that
                // after a statistic reset, delta(count) > 0. In this case, delta(total) may be negative, so that
                // we end up with a negative measurement. Therefore we also compare the startTime values to check
                // that the two statistics have the same baseline.
                if (prevStatistic == null || currentStatistic.getStartTime() != prevStatistic.getStartTime()) {
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
        if (statisticsToEnable != null) {
            server.enableStatistics(descriptor, statisticsToEnable);
        }
    }

    protected double getValue(String name, WSRangeStatistic statistic) {
        return statistic.getCurrent();
    }
}
