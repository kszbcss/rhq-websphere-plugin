package be.fgov.kszbcss.websphere.rhq;

import java.util.Set;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.websphere.rhq.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.websphere.rhq.measurement.PMIMeasurementHandler;

import com.ibm.websphere.pmi.stat.WSRangeStatistic;

public class ThreadPoolComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<WebSphereServerComponent> context = getResourceContext();
        WebSphereServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(server);
        MBean mbean = new MBean(getServer(), Utils.createObjectName("WebSphere:type=ThreadPool,name=" + context.getResourceKey() + ",*"));
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(mbean) {
            @Override
            protected double getValue(String name, WSRangeStatistic statistic) {
                if (name.equals("PercentMaxed")) {
                    return ((double)statistic.getCurrent())/100;
                } else {
                    return super.getValue(name, statistic);
                }
            }
        });
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public AvailabilityType getAvailability() {
        // TODO Auto-generated method stub
        return AvailabilityType.UP;
    }

    public void stop() {
    }
}
