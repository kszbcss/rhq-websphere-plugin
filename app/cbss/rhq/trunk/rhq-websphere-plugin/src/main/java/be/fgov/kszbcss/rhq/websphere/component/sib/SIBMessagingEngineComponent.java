package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.Set;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.server.WebSphereServerComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.proxy.HAManager;
import be.fgov.kszbcss.rhq.websphere.proxy.SIBMain;
import be.fgov.kszbcss.rhq.websphere.proxy.SIBMessagingEngine;
import be.fgov.kszbcss.rhq.websphere.support.measurement.JMXOperationMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;

import com.ibm.websphere.hamanager.jmx.GroupMemberData;
import com.ibm.websphere.hamanager.jmx.GroupMemberState;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.wsspi.hamanager.GroupName;

public class SIBMessagingEngineComponent extends WebSphereServiceComponent<WebSphereServerComponent> implements MeasurementFacet {
    private static final Log log = LogFactory.getLog(SIBMessagingEngineComponent.class);
    
    private MeasurementFacetSupport measurementFacetSupport;
    private SIBMain sibMain;
    private SIBMessagingEngine sibMessagingEngine;
    private HAManager haManager;
    private String name;
    private String cachedState;
    private long cachedStateTimestamp;
    private GroupName groupName;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        name = getResourceContext().getResourceKey();
        WebSphereServer server = getServer();
        sibMain = server.getMBeanClient("WebSphere:type=SIBMain,*").getProxy(SIBMain.class);
        haManager = server.getMBeanClient("WebSphere:type=HAManager,*").getProxy(HAManager.class);
        MBeanClient sibMessagingEngineMBeanClient = server.getMBeanClient("WebSphere:type=SIBMessagingEngine,name=" + name + ",*");
        sibMessagingEngine = sibMessagingEngineMBeanClient.getProxy(SIBMessagingEngine.class);
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("health", new JMXOperationMeasurementHandler(sibMessagingEngineMBeanClient, "getHealth", true));
    }

    /**
     * Get the messaging engine name.
     * 
     * @return the messaging engine name
     */
    public String getName() {
        return name;
    }

    public SIBMessagingEngine getSibMessagingEngine() {
        return sibMessagingEngine;
    }

    public SIBMessagingEngineInfo getInfo(boolean immediate) throws InterruptedException, JMException, ConnectorException {
        ManagedServer server = getServer();
        for (SIBMessagingEngineInfo me : server.queryConfig(new SIBMessagingEngineQuery(server.getNode(), server.getServer()), immediate)) {
            if (me.getName().equals(name)) {
                return me;
            }
        }
        return null;
    }
    
    private synchronized GroupName getGroupName() throws InterruptedException, JMException, ConnectorException {
        if (groupName == null) {
            ManagedServer server = getServer();
            SIBMessagingEngineInfo info = getInfo(false);
            groupName = haManager.createGroupName(GroupName.WAS_CLUSTER + "=" + server.getClusterName()
                    + ",WSAF_SIB_BUS=" + info.getBusName() + ",WSAF_SIB_MESSAGING_ENGINE=" + info.getName() + ",type=WSAF_SIB");
        }
        return groupName;
    }
    
    @Override
    protected boolean isConfigured(boolean immediate) throws Exception {
        return getInfo(immediate) != null;
    }

    protected AvailabilityType doGetAvailability() {
        if (log.isDebugEnabled()) {
            log.debug("Starting to determine availability of messaging engine " + name);
        }
        String state;
        try {
            state = getState();
        } catch (Exception ex) {
            log.debug("Failed to get messaging engine state => messaging engine DOWN", ex);
            return AvailabilityType.DOWN;
        }
        if (log.isDebugEnabled()) {
            log.debug("state = " + state);
        }
        if (state == null) {
            // We get here if SIBMain#showMessagingEngines doesn't list the messaging engine
            log.debug("Failed to get messaging engine state => messaging engine DOWN");
            return AvailabilityType.DOWN;
        } else if (state.equals("Stopped") || state.equals("Starting")) {
            if (log.isDebugEnabled()) {
                log.debug("Messaging engine is in state " + state + " => messaging engine DOWN");
            }
            return AvailabilityType.DOWN;
        } else if (state.equals("Started")) {
            String health;
            try {
                health = sibMessagingEngine.getHealth();
            } catch (Exception ex) {
                log.debug("Failed to get messaging engine health => messaging engine DOWN", ex);
                return AvailabilityType.DOWN;
            }
            AvailabilityType availability = health.equals("State=OK") ? AvailabilityType.UP : AvailabilityType.DOWN;
            if (log.isDebugEnabled()) {
                log.debug("health = " + health + " => message engine " + availability);
            }
            return availability;
        } else if (state.equals("Joined")) {
            log.debug("Messaging engine is in state Joined; check the state in the HAManager");
            try {
                ManagedServer server = getServer();
                String nodeName = server.getNode();
                String serverName = server.getServer();
                long startTime = System.currentTimeMillis();
                GroupMemberData[] members = haManager.retrieveGroupMembers(getGroupName());
                long duration = System.currentTimeMillis() - startTime;
                if (log.isDebugEnabled()) {
                    log.debug("HAManager#retrieveGroupMembers took " + duration + " ms");
                }
                for (GroupMemberData member : members) {
                    if (member.getNodeName().equals(nodeName) && member.getServerName().equals(serverName)) {
                        GroupMemberState memberState = member.getMemberState();
                        AvailabilityType availability = memberState.equals(GroupMemberState.IDLE) ? AvailabilityType.UP : AvailabilityType.DOWN;
                        if (log.isDebugEnabled()) {
                            log.debug("Group member state = " + memberState + " => messaging engine " + availability);
                        }
                        return availability;
                    }
                }
                log.debug("No group member data found => messaging engine DOWN");
                return AvailabilityType.DOWN;
            } catch (Exception ex) {
                log.debug("Failed to get state from HAManager => messaging engine DOWN", ex);
                return AvailabilityType.DOWN;
            }
        } else {
            log.error("Unknown state " + state + " for messaging engine " + name);
            return AvailabilityType.DOWN;
        }
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    private synchronized String getState() throws JMException, ConnectorException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - cachedStateTimestamp > 60000) {
            cachedState = null;
            for (String line: sibMain.showMessagingEngines()) {
                String[] parts = line.split(":");
                if (parts[1].equals(name)) {
                    cachedState = parts[2];
                    break;
                }
            }
            cachedStateTimestamp = currentTime;
        }
        return cachedState;
    }
    
    public boolean isActive() throws JMException, ConnectorException {
        return "Started".equals(getState());
    }
    
    @Override
    protected OperationResult doInvokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("enable")) {
            ManagedServer server = getServer();
            haManager.enableMember(getGroupName(), server.getNode(), server.getServer());
            return null;
        } else if (name.equals("disable")) {
            ManagedServer server = getServer();
            haManager.disableMember(getGroupName(), server.getNode(), server.getServer());
            return null;
        } else {
            return null;
        }
    }

    public void stop() {
    }
}
