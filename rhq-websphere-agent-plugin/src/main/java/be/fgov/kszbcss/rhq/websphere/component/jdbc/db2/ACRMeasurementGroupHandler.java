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
        if (log.isDebugEnabled()) {
            log.debug("serverList = " + serverList);
        }
        for (Map.Entry<String,MeasurementScheduleRequest> request : requests.entrySet()) {
            String name = request.getKey();
            if (name.equals("primary")) {
                if (log.isDebugEnabled()) {
                    log.debug("primaryServerName = " + serverList.getPrimaryServerName());
                    log.debug("primaryPortNumber = " + serverList.getPrimaryPortNumber());
                }
                report.addData(new MeasurementDataTrait(request.getValue(), serverList.getPrimaryServerName() + ":" + serverList.getPrimaryPortNumber()));
            } else if (name.equals("alternate")) {
                String[] serverNames = serverList.getAlternateServerName();
                String value;
                if (serverNames == null) {
                    log.debug("alternateServerName is null => reporting empty value for 'alternate'");
                    value = "";
                } else {
                    int[] ports = serverList.getAlternatePortNumber();
                    if (log.isDebugEnabled()) {
                        log.debug("alternateServerName = " + serverNames);
                        log.debug("alternatePortNumber = " + ports);
                    }
                    StringBuilder buffer = new StringBuilder();
                    for (int i=0; i<serverNames.length; i++) {
                        if (buffer.length() > 0) {
                            buffer.append(',');
                        }
                        buffer.append(serverNames[i]);
                        buffer.append(':');
                        buffer.append(ports[i]);
                    }
                    value = buffer.toString();
                    if (log.isDebugEnabled()) {
                        log.debug("Reporting '" + value + "' for 'alternate'");
                    }
                }
                report.addData(new MeasurementDataTrait(request.getValue(), value));
            }
        }
    }
}