package be.fgov.kszbcss.websphere.rhq.support.measurement;

import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.websphere.rhq.WebSphereServer;

/**
 * Handles an individual measurement on behalf of {@link MeasurementFacetSupport}.
 */
public interface MeasurementHandler {
    void getValue(WebSphereServer server, MeasurementReport report, MeasurementScheduleRequest request);
}
