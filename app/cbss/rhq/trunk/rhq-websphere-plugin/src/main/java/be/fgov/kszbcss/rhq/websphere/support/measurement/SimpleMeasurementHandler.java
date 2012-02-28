package be.fgov.kszbcss.rhq.websphere.support.measurement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

public abstract class SimpleMeasurementHandler implements MeasurementHandler {
    private static final Log log = LogFactory.getLog(SimpleMeasurementHandler.class);
    
    public final void getValue(WebSphereServer server, MeasurementReport report, MeasurementScheduleRequest request) {
        try {
            Object value = getValue();
            if (value != null) {
                JMXMeasurementDataUtils.addData(report, request, value);
            }
        } catch (Exception ex) {
            log.error("Failed to get value for " + request.getName(), ex);
        }
    }
    
    protected abstract Object getValue() throws Exception;
}
