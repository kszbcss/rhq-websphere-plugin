package be.fgov.kszbcss.rhq.websphere.support.measurement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

public class JMXMeasurementDataUtils {
    private static final Log log = LogFactory.getLog(JMXMeasurementDataUtils.class);
    
    private JMXMeasurementDataUtils() {}
    
    public static void addData(MeasurementReport report, MeasurementScheduleRequest request, Object value) {
        switch (request.getDataType()) {
            case MEASUREMENT:
                Double doubleValue;
                if (value instanceof Double) {
                    doubleValue = (Double)value;
                } else if (value instanceof Number) {
                    doubleValue = Double.valueOf(((Number)value).doubleValue());
                } else {
                    log.error("Type " + value.getClass() + " not support for numeric measurements");
                    break;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Adding (numeric) measurement for " + request.getName() + "; value=" + value);
                }
                report.addData(new MeasurementDataNumeric(request, doubleValue));
                break;
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