package be.fgov.kszbcss.rhq.websphere.component;

import java.util.Set;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.stat.WSRangeStatistic;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIModuleSelector;

public abstract class ConnectionFactoryComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet {
    private static final Log log = LogFactory.getLog(ConnectionFactoryComponent.class);
    
    protected String jndiName;
    protected MBeanClient mbean;
    private MeasurementFacetSupport measurementFacetSupport;

    protected abstract ConnectionFactoryType getType();

    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        jndiName = getResourceContext().getResourceKey();
        final ManagedServer server = getServer();
        mbean = server.getMBeanClient(new ConnectionFactoryMBeanLocator(getType(), jndiName));
        measurementFacetSupport = new MeasurementFacetSupport(this);
        PMIModuleSelector moduleSelector = new PMIModuleSelector() {
            public String[] getPath() throws JMException, ConnectorException {
                ConnectionFactoryInfo cf = server.queryConfig(new ConnectionFactoryQuery(server.getNode(), server.getServer(), getType())).getByJndiName(jndiName);
                return new String[] { getType().getPmiModule(), cf.getProviderName(), cf.getJndiName() };
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
    
    public void stop() {
    }

    public ConnectionFactoryInfo getConnectionFactoryInfo() throws JMException, ConnectorException {
        ManagedServer server = getServer();
        return server.queryConfig(new ConnectionFactoryQuery(server.getNode(), server.getServer(), getType())).getByJndiName(jndiName);
    }
    
    public AvailabilityType getAvailability() {
        if (log.isDebugEnabled()) {
            log.debug("Starting to determine availability of " + jndiName);
        }
        try {
            mbean.getObjectName(true);
            if (log.isDebugEnabled()) {
                log.debug("MBean found => availability == UP");
            }
            return AvailabilityType.UP;
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("MBean not found => availability == DOWN", ex);
            }
            return AvailabilityType.DOWN;
        }
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }
}
