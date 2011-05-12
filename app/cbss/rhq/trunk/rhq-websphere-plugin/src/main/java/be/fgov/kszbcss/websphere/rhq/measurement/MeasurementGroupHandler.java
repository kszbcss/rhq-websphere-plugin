package be.fgov.kszbcss.websphere.rhq.measurement;

import java.util.Map;

import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.websphere.rhq.WebSphereServer;

/**
 * Handles a group of measurements on behalf of {@link MeasurementFacetSupport}.
 */
public interface MeasurementGroupHandler {
    void getValues(WebSphereServer server, MeasurementReport report, Map<String,MeasurementScheduleRequest> requests);
}
