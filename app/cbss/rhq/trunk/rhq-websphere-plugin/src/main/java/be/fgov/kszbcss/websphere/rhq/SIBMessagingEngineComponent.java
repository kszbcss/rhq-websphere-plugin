package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;

import com.ibm.websphere.management.exception.ConnectorException;

public class SIBMessagingEngineComponent extends WebSphereServiceComponent<WebSphereServerComponent> {
    private MBean sibMainMBean;
    private MBean sibMessagingEngineMBean;
    private String mame;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        mame = getResourceContext().getResourceKey();
        sibMainMBean = new MBean(getServer(), Utils.createObjectName("WebSphere:type=SIBMain,*"));
        sibMessagingEngineMBean = new MBean(getServer(), Utils.createObjectName("WebSphere:type=SIBMessagingEngine,*"));
    }

    public AvailabilityType getAvailability() {
        String state;
        try {
            state = getState();
        } catch (Exception ex) {
            return AvailabilityType.DOWN;
        }
        if (state == null) {
            return AvailabilityType.DOWN;
        } else if (state.equals("Started")) {
            String health;
            try {
                health = (String)sibMessagingEngineMBean.invoke("getHealth", new Object[0], new String[0]);
            } catch (Exception ex) {
                return AvailabilityType.DOWN;
            }
            return health.equals("State=OK") ? AvailabilityType.UP : AvailabilityType.DOWN;
        } else if (state.equals("Joined")) {
            return AvailabilityType.UP;
        } else {
            return AvailabilityType.DOWN;
        }
    }

    private String getState() throws JMException, ConnectorException {
        for (String line: (String[])sibMainMBean.invoke("showMessagingEngines", new Object[0], new String[0])) {
            String[] parts = line.split(":");
            if (parts[1].equals(mame)) {
                return parts[2];
            }
        }
        return null;
    }
    
    public void stop() {
    }
}
