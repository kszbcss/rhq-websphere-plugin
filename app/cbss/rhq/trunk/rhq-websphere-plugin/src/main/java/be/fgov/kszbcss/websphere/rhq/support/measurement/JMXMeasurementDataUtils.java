package be.fgov.kszbcss.websphere.rhq.support.measurement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

public class JMXMeasurementDataUtils {
    private static final Log log = LogFactory.getLog(JMXMeasurementDataUtils.class);
    
    private JMXMeasurementDataUtils() {}
    
    public static void addData(MeasurementReport report, MeasurementScheduleRequest request, Object value) {
        switch (request.getDataType()) {
            case TRAIT:
                if (log.isDebugEnabled()) {
                    log.debug("Adding measurement (trait) for " + request.getName() + "; value=" + value);
                }
                report.addData(new MeasurementDataTrait(request, value == null ? null : value.toString()));
                break;
            default:
                log.error("Data type " + request.getDataType() + " not supported");
        }
    }
}
