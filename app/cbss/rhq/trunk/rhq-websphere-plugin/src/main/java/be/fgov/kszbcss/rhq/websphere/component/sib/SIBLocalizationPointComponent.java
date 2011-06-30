package be.fgov.kszbcss.rhq.websphere.component.sib;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;

import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;

public abstract class SIBLocalizationPointComponent extends WebSphereServiceComponent<SIBMessagingEngineComponent> {
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        // TODO Auto-generated method stub
        
    }

    public AvailabilityType getAvailability() {
        // TODO Auto-generated method stub
        return null;
    }

    public void stop() {
        // TODO Auto-generated method stub
        
    }
}
