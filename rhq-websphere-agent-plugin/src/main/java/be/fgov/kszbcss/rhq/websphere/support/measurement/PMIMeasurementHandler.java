/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2014 Crossroads Bank for Social Security
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package be.fgov.kszbcss.rhq.websphere.support.measurement;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.rhq.websphere.process.WebSphereServer;

import com.ibm.websphere.pmi.PmiDataInfo;
import com.ibm.websphere.pmi.PmiModuleConfig;
import com.ibm.websphere.pmi.stat.StatDescriptor;
import com.ibm.websphere.pmi.stat.WSAverageStatistic;
import com.ibm.websphere.pmi.stat.WSCountStatistic;
import com.ibm.websphere.pmi.stat.WSRangeStatistic;
import com.ibm.websphere.pmi.stat.WSStatistic;
import com.ibm.websphere.pmi.stat.WSStats;

public class PMIMeasurementHandler implements MeasurementGroupHandler {
    private static long STAT_ENABLE_ATTEMPT_INTERVAL = 12*3600*1000;
    
    private static final Logger log = LoggerFactory.getLogger(PMIMeasurementHandler.class);
    
    private final PMIModuleSelector moduleSelector;
    private StatDescriptor descriptor;
    private final Map<String,WSAverageStatistic> lastStats = new HashMap<String,WSAverageStatistic>();
    
    /**
     * Records failed attempts to enable a statistic. The key is the dataId of the statistic, and
     * the value is the timestamp of the last attempt to enable the statistic.
     */
    private final Map<Integer,Long> failedAttemptsToEnableStat = new HashMap<Integer,Long>();
    
    public PMIMeasurementHandler(String... path) {
        this(new StaticPMIModuleSelector(path));
    }
    
    public PMIMeasurementHandler(PMIModuleSelector moduleSelector) {
        this.moduleSelector = moduleSelector;
    }

    private void purgeFailedAttemptsToEnableStat() {
        long currentTime = System.currentTimeMillis();
        for (Iterator<Long> it = failedAttemptsToEnableStat.values().iterator(); it.hasNext(); ) {
            // Purge every 12h
            if (currentTime > it.next() + STAT_ENABLE_ATTEMPT_INTERVAL) {
                it.remove();
            }
        }
    }
    
    private StatDescriptor loadDescriptor() {
        try {
            return new StatDescriptor(moduleSelector.getPath());
        } catch (Exception ex) {
            log.error("Failed to determine PMI module path", ex);
            return null;
        }
    }
    
    public void getValues(WebSphereServer server, MeasurementReport report, Map<String,MeasurementScheduleRequest> requests) {
        purgeFailedAttemptsToEnableStat();
        
        StatDescriptor descriptor;
        WSStats stats;
        boolean isFreshDescriptor;
        synchronized (this) {
            descriptor = this.descriptor;
            if (descriptor == null) {
                log.debug("Initial load of descriptor");
                descriptor = loadDescriptor();
                if (descriptor == null) {
                    return;
                }
                this.descriptor = descriptor;
                isFreshDescriptor = true;
            } else {
                isFreshDescriptor = false;
            }
        }
        while (true) {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to get stats for " + descriptor);
            }
            try {
                stats = server.getWSStats(descriptor);
            } catch (Exception ex) {
                log.error("Failed to get statistics object", ex);
                return;
            }
            if (stats == null) {
                if (isFreshDescriptor) {
                    log.debug("Stats not available");
                    return;
                } else {
                    synchronized (this) {
                        log.debug("Refreshing descriptor");
                        descriptor = loadDescriptor();
                        if (Arrays.equals(this.descriptor.getPath(), descriptor.getPath())) {
                            log.debug("Descriptor didn't change; abandon.");
                            return;
                        } else {
                            log.debug("Descriptor changed; retrying...");
                            this.descriptor = descriptor;
                            isFreshDescriptor = true;
                        }
                    }
                }
            } else {
                break;
            }
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
                if (log.isDebugEnabled()) {
                	log.debug("Could not find statistic with name " + statisticName + " as is (stats type " + stats.getStatsType() + ") in the PMI module configuration; attempting to find a matching prefixed name among the existing statistics");
                }
                // WebSphere 8.5 prefixes some stat names with a given string within a given type;
                // most of the time, this prefix will be the shortened type name (i.e. QueueStats),
                // but sometimes it isn't, which makes generalization impossible. The prefixed format being
                // a stable <Prefix>.<StatisticName>, we will seek for a ".statisticName" in all
                // available PmiDataInfo in the considered pmiConfigModule.
                for (PmiDataInfo pdi : pmiModuleConfig.listAllData()) {
                	if (pdi.getName().endsWith("." + statisticName)) {
                		dataId = pdi.getId();
                		break;
                	}
                }
            }
            if (dataId == -1) {
                log.error("Could not find statistic with name " + statisticName + " (stats type " + stats.getStatsType() + ") in the PMI module configuration");
                continue;
            }
            // For some WSStats objects, the statistic names don't match the names used by PMI.
            // Therefore we translate all names to data IDs. This also makes it easier to
            // automatically enable the statistics if necessary.
            WSStatistic statistic = stats.getStatistic(dataId);
            if (statistic == null) {
                if (failedAttemptsToEnableStat.containsKey(dataId)) {
                    if (log.isDebugEnabled()) {
                        Date nextAttempt = new Date(failedAttemptsToEnableStat.get(dataId) + STAT_ENABLE_ATTEMPT_INTERVAL);
                        log.debug("Statistic with name " + statisticName + " (ID " + dataId + ") not available, but a previous attempt to enable the statistic failed; will retry at " + SimpleDateFormat.getInstance().format(nextAttempt));
                    }
                } else {
                    log.info("Statistic with name " + statisticName + " (ID " + dataId + ") not available; will attempt to enable it");
                    if (statisticsToEnable == null) {
                        statisticsToEnable = new HashSet<Integer>();
                    }
                    statisticsToEnable.add(dataId);
                }
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
                if (log.isDebugEnabled()) {
                    if (prevStatistic == null) {
                        log.debug("Previous value: <not available>");
                    } else {
                        log.debug("Previous value: " + dumpStatistic(prevStatistic));
                    }
                    log.debug("Current value: " + dumpStatistic(currentStatistic));
                }
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
            statisticsToEnable.removeAll(server.enableStatistics(descriptor, statisticsToEnable));
            if (!statisticsToEnable.isEmpty()) {
                Long currentTime = System.currentTimeMillis();
                for (Integer dataId : statisticsToEnable) {
                    failedAttemptsToEnableStat.put(dataId, currentTime);
                }
                if (log.isDebugEnabled()) {
                    log.debug("failedAttemptsToEnable = " + failedAttemptsToEnableStat);
                }
            }
        }
    }
    
    private static String dumpStatistic(WSAverageStatistic statistic) {
        return "total=" + statistic.getTotal() + ", min=" + statistic.getMin() + ", max=" + statistic.getMax() + ", startTime=" + statistic.getStartTime() + ", count=" + statistic.getCount();
    }
    
    protected double getValue(String name, WSRangeStatistic statistic) {
        return statistic.getCurrent();
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + moduleSelector + "]";
    }
}
