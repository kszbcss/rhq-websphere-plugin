package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;

public class ServletComponent extends PMIComponent<WebModuleComponent> {
    private MBean mbean;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<WebModuleComponent> context = getResourceContext();
        ModuleComponent parent = context.getParentResourceComponent();
        mbean = new MBean(getServer(), Utils.createObjectName("WebSphere:type=Servlet,Application=" + parent.getApplicationName() + ",WebModule=" + parent.getModuleName() + ",name=" + context.getResourceKey() + ",*"));
    }

    @Override
    protected MBeanStatDescriptor getMBeanStatDescriptor() throws JMException, ConnectorException {
        return new MBeanStatDescriptor(mbean.getObjectName());
    }

    public AvailabilityType getAvailability() {
        // TODO Auto-generated method stub
        return AvailabilityType.UP;
    }

    public void stop() {
    }
}
