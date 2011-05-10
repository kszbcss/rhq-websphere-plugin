package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;

public abstract class EnterpriseBeanComponent extends PMIComponent<EJBModuleComponent> {
    private MBean mbean;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<EJBModuleComponent> context = getResourceContext();
        ModuleComponent parent = context.getParentResourceComponent();
        mbean = new MBean(getServer(), Utils.createObjectName("WebSphere:type=" + getMBeanType() + ",Application=" + parent.getApplicationName() + ",EJBModule=" + parent.getModuleName() + ",name=" + context.getResourceKey() + ",*"));
    }
    
    protected abstract String getMBeanType();
    
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
