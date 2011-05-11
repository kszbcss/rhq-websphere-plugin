package be.fgov.kszbcss.websphere.rhq;

import java.util.Set;

import javax.management.JMException;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.websphere.rhq.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.websphere.rhq.measurement.PMIMeasurementHandler;
import be.fgov.kszbcss.websphere.rhq.measurement.PMIModuleSelector;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.stat.WSRangeStatistic;

public class DataSourceComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        final String jndiName = getResourceContext().getResourceKey();
        WebSphereServer server = getServer();
        final MBean mbean = new MBean(server, new MBeanAttributeMatcherLocator(Utils.createObjectName("WebSphere:type=DataSource,*"), "jndiName", jndiName));
        measurementFacetSupport = new MeasurementFacetSupport(server);
        PMIModuleSelector moduleSelector = new PMIModuleSelector() {
            public String[] getPath() throws JMException, ConnectorException {
                String providerName = mbean.getObjectName().getKeyProperty("JDBCProvider");
                return new String[] { "connectionPoolModule", providerName, jndiName };
            }
        };
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(), moduleSelector) {
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

    public void stop() {
    }

    public AvailabilityType getAvailability() {
        // TODO
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }
}
