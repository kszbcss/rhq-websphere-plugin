package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.Arrays;
import java.util.Set;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;

public abstract class SIBLocalizationPointComponent extends WebSphereServiceComponent<SIBMessagingEngineComponent> implements MeasurementFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ManagedServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(),
                "SIB Service", "SIB Messaging Engines", getResourceContext().getParentResourceComponent().getName(),
                "Destinations", getPMIModuleName(), getResourceContext().getResourceKey()));
    }
    
    protected abstract SIBLocalizationPointType getType();
    protected abstract String getPMIModuleName();

    @Override
    protected boolean isConfigured(boolean immediate) throws Exception {
        ManagedServer server = getServer();
        SIBMessagingEngineInfo meInfo = null;
        for (SIBMessagingEngineInfo info : server.queryConfig(new SIBMessagingEngineQuery(server.getNode(), server.getServer()), immediate)) {
            if (info.getName().equals(getResourceContext().getParentResourceComponent().getName())) {
                meInfo = info;
            }
        }
        if (meInfo == null) {
            return false;
        }
        return Arrays.asList(meInfo.getDestinationNames(getType())).contains(getResourceContext().getResourceKey());
    }
    
    protected AvailabilityType doGetAvailability() {
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        if (getResourceContext().getParentResourceComponent().isActive()) {
            measurementFacetSupport.getValues(report, requests);
        }
    }

    public void stop() {
    }
}
