package be.fgov.kszbcss.rhq.websphere.support.measurement;

import javax.management.JMException;

import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

import com.ibm.websphere.management.exception.ConnectorException;

public abstract class SimpleMeasurementHandler implements MeasurementHandler {
    public final void getValue(WebSphereServer server, MeasurementReport report, MeasurementScheduleRequest request) throws InterruptedException, JMException, ConnectorException {
        Object value = getValue();
        if (value != null) {
            JMXMeasurementDataUtils.addData(report, request, value);
        }
    }
    
    protected abstract Object getValue() throws InterruptedException, JMException, ConnectorException;
}
