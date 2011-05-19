package be.fgov.kszbcss.websphere.rhq.connector.agent;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.domain.measurement.calltime.CallTimeData;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.websphere.rhq.connector.AdminClientStats;
import be.fgov.kszbcss.websphere.rhq.connector.AdminClientStatsCollector;
import be.fgov.kszbcss.websphere.rhq.connector.AdminClientStatsData;

public class ConnectorSubsystemComponent implements ResourceComponent<ResourceComponent<?>>, MeasurementFacet {
    private static final Log log = LogFactory.getLog(ConnectorSubsystemComponent.class);
    
    public void start(ResourceContext<ResourceComponent<?>> context)
            throws InvalidPluginConfigurationException, Exception {
    }

    public AvailabilityType getAvailability() {
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        log.debug("Entering getValues");
        boolean dataAdded = false;
        for (MeasurementScheduleRequest request : requests) {
            if (request.getName().equals("InvocationTime")) {
                AdminClientStats stats = AdminClientStatsCollector.INSTANCE.rotateStats();
                CallTimeData data = new CallTimeData(request);
                for (AdminClientStatsData statsData : stats.getData()) {
                    data.addAggregatedCallData(statsData.getDestination(), stats.getBeginTime(), stats.getEndTime(),
                            statsData.getMin(), statsData.getMax(), statsData.getTotal(), statsData.getCount());
                }
                report.addData(data);
                if (log.isDebugEnabled()) {
                    log.debug("Added " + data.getValues().size() + " call time data items to the report");
                }
                dataAdded = true;
            }
        }
        if (!dataAdded) {
            log.debug("No call time data requested. Nothing has been added to the report.");
        }
    }

    public void stop() {
    }
}
