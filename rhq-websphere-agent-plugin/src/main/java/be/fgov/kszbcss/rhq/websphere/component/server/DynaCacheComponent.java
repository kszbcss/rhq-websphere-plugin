package be.fgov.kszbcss.rhq.websphere.component.server;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.proxy.DynaCache;

public class DynaCacheComponent implements ResourceComponent<WebSphereServerComponent>, MeasurementFacet, OperationFacet {
    private static final Log log = LogFactory.getLog(DynaCacheComponent.class);
    
    private DynaCache cache;
    private String instanceName;

    public void start(ResourceContext<WebSphereServerComponent> context) throws InvalidPluginConfigurationException, Exception {
        cache = context.getParentResourceComponent().getServer().getMBeanClient("WebSphere:type=DynaCache,*").getProxy(DynaCache.class);
        instanceName = context.getResourceKey();
    }

    public AvailabilityType getAvailability() {
        String[] instanceNames;
        try {
            instanceNames = cache.getCacheInstanceNames();
        } catch (Exception ex) {
            log.error("Unable to get cache instance names", ex);
            return AvailabilityType.DOWN;
        }
        for (String name : instanceNames) {
            if (name.equals(instanceName)) {
                return AvailabilityType.UP;
            }
        }
        return AvailabilityType.DOWN;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        String[] stats = cache.getAllCacheStatistics(instanceName);
        for (MeasurementScheduleRequest request : requests) {
            String name = request.getName();
            String value = null;
            for (String stat : stats) {
                if (stat.indexOf('=') == name.length() && stat.startsWith(name)) {
                    value = stat.substring(name.length()+1);
                    break;
                }
            }
            if (value != null) {
                report.addData(new MeasurementDataNumeric(request, Double.parseDouble(value)));
            }
        }
    }

    public OperationResult invokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("clearCache")) {
            cache.clearCache(instanceName);
        }
        return null;
    }

    public void stop() {
    }
}