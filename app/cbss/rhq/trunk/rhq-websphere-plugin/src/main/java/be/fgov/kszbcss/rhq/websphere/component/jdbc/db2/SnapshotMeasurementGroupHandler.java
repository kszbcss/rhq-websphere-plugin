package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.proxy.AdminOperations;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementGroupHandler;

/**
 * Collects metrics from the DB2 snapshot views.
 */
public class SnapshotMeasurementGroupHandler implements MeasurementGroupHandler {
    private static final Log log = LogFactory.getLog(SnapshotMeasurementGroupHandler.class);
    
    private static final Map<String,String> expressions;
    
    static {
        expressions = new HashMap<String,String>();
        expressions.put("agent_usr_cpu_time", "agent_usr_cpu_time_s*1000+agent_usr_cpu_time_ms/1000");
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
            ConnectionContext connectionContext = monitor.getConnectionContext();
            Map<String,Object> dataSourceProps = connectionContext.getDataSourceProperties();
            
            // TODO: this should be done in ConnectionContext!
            String clientProgramName = (String)dataSourceProps.get("clientProgramName");
            if (clientProgramName == null || clientProgramName.length() == 0) {
                log.warn("clientProgramName not configured; unable to correlate snapshot data");
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
            connectionContext.execute(new Query<Void>() {
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
                DB2BaseMetricData adjusted = baseMetricData.get(name);
                if (adjusted == null) {
                    baseMetricData.put(name, raw);
                    adjusted = raw;
                } else {
                    adjusted.update(raw);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Processing data for " + name + "; raw value: " + raw.getSum() + "; adjusted value: " + adjusted.getSum());
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
