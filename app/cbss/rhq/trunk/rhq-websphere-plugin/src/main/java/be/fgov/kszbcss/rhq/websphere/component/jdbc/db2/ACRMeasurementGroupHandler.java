package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementGroupHandler;

import com.ibm.db2.jcc.DB2ClientRerouteServerList;

public class ACRMeasurementGroupHandler implements MeasurementGroupHandler {
    private static final Log log = LogFactory.getLog(ACRMeasurementGroupHandler.class);
    
    private final DB2MonitorComponent monitor;

    public ACRMeasurementGroupHandler(DB2MonitorComponent monitor) {
        this.monitor = monitor;
    }

    public void getValues(WebSphereServer server, MeasurementReport report, Map<String,MeasurementScheduleRequest> requests) {
        DB2ClientRerouteServerList serverList;
        try {
            serverList = monitor.getContext().getClientRerouteServerList();
        } catch (Exception ex) {
            log.error("Failed to get client reroute server list", ex);
            return;
        }
        for (Map.Entry<String,MeasurementScheduleRequest> request : requests.entrySet()) {
            String name = request.getKey();
            if (name.equals("primary")) {
                report.addData(new MeasurementDataTrait(request.getValue(), serverList.getPrimaryServerName() + ":" + serverList.getPrimaryPortNumber()));
            } else if (name.equals("alternate")) {
                String[] serverNames = serverList.getAlternateServerName();
                int[] ports = serverList.getAlternatePortNumber();
                StringBuilder buffer = new StringBuilder();
                for (int i=0; i<serverNames.length; i++) {
                    if (buffer.length() > 0) {
                        buffer.append(',');
                    }
                    buffer.append(serverNames[i]);
                    buffer.append(':');
                    buffer.append(ports[i]);
                }
                report.addData(new MeasurementDataTrait(request.getValue(), buffer.toString()));
            }
        }
    }
}
