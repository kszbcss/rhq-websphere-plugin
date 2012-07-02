package be.fgov.kszbcss.rhq.websphere.component.xm4was;

import java.util.Set;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;

public abstract class ModuleClassLoaderStatsComponent<T extends WebSphereServiceComponent<?>> extends WebSphereServiceComponent<T> implements MeasurementFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(getServer().getServerMBean(), "ClassLoaderStats", getModuleName()));
    }

    @Override
    protected boolean isConfigured() throws Exception {
        return true;
    }

    @Override
    protected AvailabilityType doGetAvailability() {
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public void stop() {
    }

    protected abstract String getModuleName();
}
