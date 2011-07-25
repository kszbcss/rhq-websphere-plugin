package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.component.jdbc.DataSourceComponent;
import be.fgov.kszbcss.rhq.websphere.proxy.AdminOperations;

public class DB2MonitorComponent implements ResourceComponent<DataSourceComponent>, MeasurementFacet {
    private static final Log log = LogFactory.getLog(DB2MonitorComponent.class);
    
    private static final Map<String,String> expressions;
    
    static {
        expressions = new HashMap<String,String>();
        expressions.put("agent_usr_cpu_time", "agent_usr_cpu_time_s*1000+agent_usr_cpu_time_ms/1000");
    }
    
    private DataSourceComponent dataSourceComponent;
    private AdminOperations adminOperations;
    private String principal;
    private String credentials;
    private Connection connection;
    private final Map<String,DB2MetricData> lastValues = new HashMap<String,DB2MetricData>();
    
    public void start(ResourceContext<DataSourceComponent> context) throws InvalidPluginConfigurationException, Exception {
        dataSourceComponent = context.getParentResourceComponent();
        adminOperations = dataSourceComponent.getServer().getMBeanClient("WebSphere:type=AdminOperations,*").getProxy(AdminOperations.class);
        Configuration config = context.getPluginConfiguration();
        principal = config.getSimpleValue("principal", null);
        credentials = config.getSimpleValue("credentials", null);
    }

    public AvailabilityType getAvailability() {
        // TODO Auto-generated method stub
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        Map<String,Object> dataSourceProps = dataSourceComponent.getConnectionFactoryInfo().getProperties();
        
        String applName = adminOperations.expandVariable((String)dataSourceProps.get("clientProgramName"));
        if (log.isDebugEnabled()) {
            log.debug("clientProgramName = " + applName);
        }
        
        if (connection == null) {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            Properties info = new Properties();
            for (Map.Entry<String,Object> entry : dataSourceProps.entrySet()) {
                Object value = entry.getValue();
                if (value != null && !value.equals("")) {
                    info.setProperty(entry.getKey(), value.toString());
                }
            }
            info.setProperty("clientProgramName", "RHQ");
            info.setProperty("user", principal);
            info.setProperty("password", credentials);
            String url = "jdbc:db2://" + info.remove("serverName") + ":" + info.remove("portNumber") + "/" + info.remove("databaseName");
            if (log.isDebugEnabled()) {
                log.debug("Attempting to connect with URL " + url + " and properties " + info);
            }
            connection = DriverManager.getConnection(url, info);
        }
        
        StringBuilder buffer = new StringBuilder("SELECT A.AGENT_ID");
        for (MeasurementScheduleRequest request : requests) {
            String name = request.getName();
            buffer.append(", ");
            String expression = expressions.get(name);
            buffer.append(expression == null ? name : expression);
        }
        buffer.append(" FROM SYSIBMADM.SNAPAPPL AS A, SYSIBMADM.SNAPAPPL_INFO AS AI WHERE A.AGENT_ID=AI.AGENT_ID AND AI.APPL_NAME=?");
        String sql = buffer.toString();
        
        if (log.isDebugEnabled()) {
            log.debug("Preparing to execute statement: " + sql);
        }
        
        DB2MetricData[] newData = new DB2MetricData[requests.size()];
        for (int i=0; i<newData.length; i++) {
            newData[i] = new DB2MetricData();
        }
        PreparedStatement stmt = connection.prepareStatement(sql);
        try {
            stmt.setString(1, applName);
            ResultSet rs = stmt.executeQuery();
            try {
                while (rs.next()) {
                    long agentId = rs.getLong(1);
                    for (int i=0; i<requests.size(); i++) {
                        newData[i].addValue(agentId, rs.getLong(i+2));
                    }
                }
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }

        int i = 0;
        for (MeasurementScheduleRequest request : requests) {
            String name = request.getName();
            if (log.isDebugEnabled()) {
                log.debug("Adding measurement for " + name);
            }
            DB2MetricData current = newData[i++];
            DB2MetricData last = lastValues.get(name);
            if (last == null) {
                lastValues.put(name, current);
            } else {
                last.update(current);
                current = last;
            }
            report.addData(new MeasurementDataNumeric(request, (double)current.getSum()));
        }
    }

    public void stop() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                log.error("Error closing database connection", ex);
            }
        }
    }
}
