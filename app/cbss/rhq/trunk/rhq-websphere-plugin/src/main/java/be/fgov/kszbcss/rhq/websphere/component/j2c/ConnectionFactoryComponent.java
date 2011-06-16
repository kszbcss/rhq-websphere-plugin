package be.fgov.kszbcss.rhq.websphere.component.j2c;

import java.util.Set;

import javax.management.JMException;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIModuleSelector;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiConstants;
import com.ibm.websphere.pmi.stat.WSRangeStatistic;

public class ConnectionFactoryComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet {
    private String jndiName;
    private MeasurementFacetSupport measurementFacetSupport;

    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        jndiName = getResourceContext().getResourceKey();
        WebSphereServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(this);
        PMIModuleSelector moduleSelector = new PMIModuleSelector() {
            public String[] getPath() throws JMException, ConnectorException {
                ConnectionFactoryInfo cf = getConnectionFactoryInfo();
                return new String[] { PmiConstants.J2C_MODULE, cf.getProviderName(), cf.getJndiName() };
            }
        };
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(), moduleSelector) {
            @Override
            protected double getValue(String name, WSRangeStatistic statistic) {
                if (name.equals("PercentMaxed") || name.equals("PercentUsed")) {
                    return ((double)statistic.getCurrent())/100;
                } else {
                    return super.getValue(name, statistic);
                }
            }
        });
    }

    ConnectionFactoryInfo getConnectionFactoryInfo() throws JMException, ConnectorException {
        ManagedServer server = getServer();
        for (ConnectionFactoryInfo cf : server.queryConfig(new ConnectionFactoryQuery(server.getNode(), server.getServer()))) {
            if (cf.getJndiName().equals(jndiName)) {
                return cf;
            }
        }
        throw new RuntimeException("Configuration object for " + jndiName + " not found");
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
