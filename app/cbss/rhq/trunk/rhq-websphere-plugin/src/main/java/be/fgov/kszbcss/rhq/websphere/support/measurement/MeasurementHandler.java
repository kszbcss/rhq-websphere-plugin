package be.fgov.kszbcss.rhq.websphere.support.measurement;

import javax.management.JMException;

import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

/**
 * Handles an individual measurement on behalf of {@link MeasurementFacetSupport}.
 */
public interface MeasurementHandler {
    void getValue(WebSphereServer server, MeasurementReport report, MeasurementScheduleRequest request) throws InterruptedException, JMException, ConnectorException;
}
