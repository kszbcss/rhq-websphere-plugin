package be.fgov.kszbcss.websphere.rhq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.plugins.jmx.MBeanResourceComponent;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiModuleConfig;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
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
    
    protected ObjectName getMBean() {
        try {
            return new ObjectName(getEmsBean().getBeanName().toString());
        } catch (MalformedObjectNameException ex) {
            throw new RuntimeException(ex); // TODO
        }
    }
    
    @Override
    protected void getValues(MeasurementReport report, Set requests, EmsBean bean) {
        Set<MeasurementScheduleRequest> simpleRequests = new HashSet<MeasurementScheduleRequest>();
        MBeanStatDescriptor descriptor = null;
        WSStats stats = null;
        PmiModuleConfig pmiModuleConfig = null;
        Set<Integer> statisticsToEnable = null;
        for (MeasurementScheduleRequest request : (Set<MeasurementScheduleRequest>)requests) {
            String name = request.getName();
            if (name.startsWith("stats.")) {
                if (log.isDebugEnabled()) {
                    log.debug("Starting to get value for " + name + " on " + getResourceContext().getResourceKey());
                }
                if (descriptor == null) {
                    descriptor = getMBeanStatDescriptor();
                    WebSphereServer server = getServer();
                    try {
                        stats = server.getWSStats(descriptor);
                        if (stats != null) {
                            pmiModuleConfig = server.getPmiModuleConfig(stats);
                        }
                    } catch (JMException ex) {
                        log.error("Failed to get statistics object", ex);
                    } catch (ConnectorException ex) {
                        log.error("Failed to get statistics object", ex);
                    }
                }
                if (stats != null) {
                    String statisticName = name.substring(6);
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
        if (statisticsToEnable != null) {
            getServer().enableStatistics(descriptor, statisticsToEnable);
        }
        if (!simpleRequests.isEmpty()) {
            super.getValues(report, simpleRequests, bean);
        }
    }

    protected MBeanStatDescriptor getMBeanStatDescriptor() {
        return Utils.getMBeanStatDescriptor(getEmsBean());
    }
    
    protected double getValue(String name, WSRangeStatistic statistic) {
        return statistic.getCurrent();
    }
}
