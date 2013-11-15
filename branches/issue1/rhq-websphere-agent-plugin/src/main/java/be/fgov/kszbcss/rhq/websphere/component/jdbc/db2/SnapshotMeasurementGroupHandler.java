/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012 Crossroads Bank for Social Security
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
package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.rhq.websphere.component.jdbc.db2.pool.Query;
import be.fgov.kszbcss.rhq.websphere.process.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.proxy.AdminOperations;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementGroupHandler;

/**
 * Collects metrics from the DB2 snapshot views.
 */
public class SnapshotMeasurementGroupHandler implements MeasurementGroupHandler {
    private static final Log log = LogFactory.getLog(SnapshotMeasurementGroupHandler.class);
    
    private static final Map<String,String> expressions;
    
    /**
     * A set of base metrics that are dynamic, i.e. which are not counters. They need to be handled
     * slightly differently, namely their values should not be retained when a connection/agent
     * disappears.
     */
    private static final Set<String> dynamicBaseMetrics = new HashSet<String>(Arrays.asList("locks_held", "uow_log_space_used"));
    
    static {
        expressions = new HashMap<String,String>();
        expressions.put("agent_usr_cpu_time", "agent_usr_cpu_time_s*1000+agent_usr_cpu_time_ms/1000");
        // uow_log_space_used is only reset to 0 when a new UOW starts, but the log space is actually freed when
        // the current UOW is committed; the following entry ensures that we only take into account uow_log_space_used
        // values for connections with an active UOW
        expressions.put("uow_log_space_used", "case when uow_stop_time is null then uow_log_space_used else 0 end");
    }
    
    private final DB2MonitorComponent monitor;
    private final AdminOperations adminOperations;
    private final Map<String,DB2BaseMetricData> baseMetricData = new HashMap<String,DB2BaseMetricData>();
    private final Map<String,DB2AverageMetricData> averageMetricData = new HashMap<String,DB2AverageMetricData>();

    public SnapshotMeasurementGroupHandler(DB2MonitorComponent monitor, AdminOperations adminOperations) {
        this.monitor = monitor;
        this.adminOperations = adminOperations;
    }

    public void getValues(WebSphereServer server, MeasurementReport report, Map<String,MeasurementScheduleRequest> requests) {
        try {
            DB2MonitorContext context = monitor.getContext();
            Map<String,Object> dataSourceProps = context.getDataSourceProperties();
            
            // TODO: this should be done in ConnectionContext!
            String clientProgramName = (String)dataSourceProps.get("clientProgramName");
            if (clientProgramName == null || clientProgramName.length() == 0) {
                log.warn("clientProgramName not configured for data source "
                        + monitor.getResourceContext().getParentResourceComponent().getResourceContext().getResourceKey()
                        + "; unable to correlate snapshot data");
                return;
            }
            final String applName = adminOperations.expandVariable(clientProgramName);
            if (log.isDebugEnabled()) {
                log.debug("clientProgramName = " + applName);
            }
            
            final Set<String> baseMetrics = new LinkedHashSet<String>();
            for (String name : requests.keySet()) {
                int idx = name.indexOf(':');
                if (idx == -1) {
                    baseMetrics.add(name);
                } else {
                    baseMetrics.add(name.substring(0, idx));
                    baseMetrics.add(name.substring(idx+1));
                }
            }
            
            if (log.isDebugEnabled()) {
                log.debug("The following base metrics will be requested from DB2: " + baseMetrics);
            }
            
            StringBuilder buffer = new StringBuilder("SELECT A.AGENT_ID");
            for (String name : baseMetrics) {
                buffer.append(", ");
                String expression = expressions.get(name);
                buffer.append(expression == null ? name : expression);
            }
            buffer.append(" FROM SYSIBMADM.SNAPAPPL AS A, SYSIBMADM.SNAPAPPL_INFO AS AI WHERE A.AGENT_ID=AI.AGENT_ID AND AI.APPL_NAME=?");
            final String sql = buffer.toString();
            
            if (log.isDebugEnabled()) {
                log.debug("Preparing to execute statement: " + sql);
            }
            
            final DB2BaseMetricData[] newData = new DB2BaseMetricData[baseMetrics.size()];
            for (int i=0; i<newData.length; i++) {
                newData[i] = new DB2BaseMetricData();
            }
            context.execute(new Query<Void>() {
                public Void execute(Connection connection) throws SQLException {
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    try {
                        stmt.setString(1, applName);
                        ResultSet rs = stmt.executeQuery();
                        try {
                            while (rs.next()) {
                                long agentId = rs.getLong(1);
                                for (int i=0; i<baseMetrics.size(); i++) {
                                    newData[i].addValue(agentId, rs.getLong(i+2));
                                }
                            }
                        } finally {
                            rs.close();
                        }
                    } finally {
                        stmt.close();
                    }
                    return null;
                }
            });
    
            int i = 0;
            for (String name : baseMetrics) {
                DB2BaseMetricData raw = newData[i++];
                if (dynamicBaseMetrics.contains(name)) {
                    baseMetricData.put(name, raw);
                    if (log.isDebugEnabled()) {
                        log.debug("Processing data for dynamic base metric " + name + "; raw value: " + raw.getSum());
                    }
                } else {
                    DB2BaseMetricData adjusted = baseMetricData.get(name);
                    if (adjusted == null) {
                        baseMetricData.put(name, raw);
                        adjusted = raw;
                    } else {
                        adjusted.update(raw);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Processing data for counter " + name + "; raw value: " + raw.getSum() + "; adjusted value: " + adjusted.getSum());
                    }
                }
            }
    
            for (Map.Entry<String,MeasurementScheduleRequest> request : requests.entrySet()) {
                String name = request.getKey();
                int idx = name.indexOf(':');
                if (idx == -1) {
                    report.addData(new MeasurementDataNumeric(request.getValue(), (double)baseMetricData.get(name).getSum()));
                } else {
                    DB2AverageMetricData current = new DB2AverageMetricData(
                            baseMetricData.get(name.substring(0, idx)).getSum(),
                            baseMetricData.get(name.substring(idx+1)).getSum());
                    DB2AverageMetricData previous = averageMetricData.put(name, current);
                    if (previous != null) {
                        long countDelta = current.getCount()-previous.getCount();
                        if (countDelta > 0) {
                            report.addData(new MeasurementDataNumeric(request.getValue(), ((double)(current.getTotal()-previous.getTotal())) / ((double)countDelta)));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Failed to collect metrics", ex);
        }
    }
}
