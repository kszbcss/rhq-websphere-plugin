package be.fgov.kszbcss.websphere.rhq;

import java.util.Set;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.websphere.rhq.mbean.MBeanClient;
import be.fgov.kszbcss.websphere.rhq.support.measurement.JMXOperationMeasurementHandler;
import be.fgov.kszbcss.websphere.rhq.support.measurement.MeasurementFacetSupport;

import com.ibm.websphere.management.exception.ConnectorException;

public class SIBMessagingEngineComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet {
    private static final Log log = LogFactory.getLog(SIBMessagingEngineComponent.class);
    
    private MeasurementFacetSupport measurementFacetSupport;
    private SIBMain sibMain;
    private SIBMessagingEngine sibMessagingEngine;
    private String name;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        name = getResourceContext().getResourceKey();
        WebSphereServer server = getServer();
        sibMain = server.getMBeanClient("WebSphere:type=SIBMain,*").getProxy(SIBMain.class);
        MBeanClient sibMessagingEngineMBeanClient = server.getMBeanClient("WebSphere:type=SIBMessagingEngine,name=" + name + ",*");
        sibMessagingEngine = sibMessagingEngineMBeanClient.getProxy(SIBMessagingEngine.class);
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("health", new JMXOperationMeasurementHandler(sibMessagingEngineMBeanClient, "getHealth", true));
    }

    public AvailabilityType getAvailability() {
        if (log.isDebugEnabled()) {
            log.debug("Starting to determine availability of messaging engine " + name);
        }
        String state;
        try {
            state = getState();
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to get messaging engine state => messaging engine DOWN", ex);
            }
            return AvailabilityType.DOWN;
        }
        if (log.isDebugEnabled()) {
            log.debug("state = " + state);
        }
        if (state == null) {
            // We get here if SIBMain#showMessagingEngines doesn't list the messaging engine
            if (log.isDebugEnabled()) {
                log.debug("Failed to get messaging engine state => messaging engine DOWN");
            }
            return AvailabilityType.DOWN;
        } else if (state.equals("Started")) {
            String health;
            try {
                health = sibMessagingEngine.getHealth();
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to get messaging engine health => messaging engine DOWN", ex);
                }
                return AvailabilityType.DOWN;
            }
            if (log.isDebugEnabled()) {
                log.debug("health = " + state);
            }
            return health.equals("State=OK") ? AvailabilityType.UP : AvailabilityType.DOWN;
        } else if (state.equals("Joined")) {
            if (log.isDebugEnabled()) {
                log.debug("Messaging engine is in state Joined => messaging engine UP");
            }
            return AvailabilityType.UP;
        } else {
            log.error("Unknown state " + state + " for messaging engine " + name);
            return AvailabilityType.DOWN;
        }
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    private String getState() throws JMException, ConnectorException {
        for (String line: sibMain.showMessagingEngines()) {
            String[] parts = line.split(":");
            if (parts[1].equals(name)) {
                return parts[2];
            }
        }
        return null;
    }
    
    public void stop() {
    }
}