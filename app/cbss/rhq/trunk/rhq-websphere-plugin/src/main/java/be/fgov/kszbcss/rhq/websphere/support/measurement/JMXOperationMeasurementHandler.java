package be.fgov.kszbcss.rhq.websphere.support.measurement;

import javax.management.InstanceNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;

public class JMXOperationMeasurementHandler implements MeasurementHandler {
    private static final Log log = LogFactory.getLog(JMXOperationMeasurementHandler.class);
    
    private final MBeanClient mbean;
    private final String operationName;
    private final boolean ignoreInstanceNotFound;

    public JMXOperationMeasurementHandler(MBeanClient mbean, String operationName, boolean ignoreInstanceNotFound) {
        this.mbean = mbean;
        this.operationName = operationName;
        this.ignoreInstanceNotFound = ignoreInstanceNotFound;
    }

    public void getValue(WebSphereServer server, MeasurementReport report, MeasurementScheduleRequest request) {
        try {
            Object value;
            try {
                value = mbean.invoke(operationName, new Object[0], new String[0]);
            } catch (InstanceNotFoundException ex) {
                if (ignoreInstanceNotFound) {
                    if (log.isDebugEnabled()) {
                        log.debug("Ignoring InstanceNotFoundException for measurement of " + request.getName() + " on " + mbean);
                    }
                    value = null;
                } else {
                    throw ex;
                }
            }
            JMXMeasurementDataUtils.addData(report, request, value);
        } catch (Exception ex) {
            log.error("Unable to get value for " + request.getName() + " on " + mbean);
        }
    }
}
