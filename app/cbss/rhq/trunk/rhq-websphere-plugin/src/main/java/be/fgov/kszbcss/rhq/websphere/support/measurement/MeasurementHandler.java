package be.fgov.kszbcss.rhq.websphere.support.measurement;

import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

/**
 * Handles an individual measurement on behalf of {@link MeasurementFacetSupport}.
 */
public interface MeasurementHandler {
    void getValue(WebSphereServer server, MeasurementReport report, MeasurementScheduleRequest request);
}
