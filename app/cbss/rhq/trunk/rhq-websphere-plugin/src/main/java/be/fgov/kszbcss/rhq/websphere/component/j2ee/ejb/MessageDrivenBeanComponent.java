package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;
import be.fgov.kszbcss.rhq.websphere.proxy.J2CMessageEndpoint;

import com.ibm.websphere.pmi.PmiConstants;

public class MessageDrivenBeanComponent extends EnterpriseBeanComponent implements OperationFacet {
    private static final Log log = LogFactory.getLog(MessageDrivenBeanComponent.class);
    
    private J2CMessageEndpoint endpoint;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        super.start();
        ResourceContext<EJBModuleComponent> context = getResourceContext();
        ModuleComponent parent = context.getParentResourceComponent();
        endpoint = getServer().getMBeanClient("WebSphere:type=J2CMessageEndpoint,name="
                + parent.getApplicationName() + "#" + parent.getModuleName() + "#"
                + context.getResourceKey() + "_J2CMessageEndpoint,*").getProxy(J2CMessageEndpoint.class);
    }

    @Override
    protected EnterpriseBeanType getType() {
        return EnterpriseBeanType.MESSAGE_DRIVEN;
    }

    @Override
    protected String getMBeanType() {
        return "MessageDrivenBean";
    }

    @Override
    protected String getPMISubmodule() {
        return PmiConstants.EJB_MESSAGEDRIVEN;
    }

    @Override
    public AvailabilityType getAvailability() {
        AvailabilityType availability = super.getAvailability();
        if (availability == AvailabilityType.DOWN) {
            return AvailabilityType.DOWN;
        } else {
            try {
                Integer status = endpoint.getStatus();
                if (log.isDebugEnabled()) {
                    log.debug("Status of J2CMessageEndpoint for MDB " + getResourceContext().getResourceKey() + ": " + status);
                }
                return status != null && status.intValue() == 1 ? AvailabilityType.UP : AvailabilityType.DOWN;
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Error getting status of J2CMessageEndpoint for MDB " + getResourceContext().getResourceKey(), ex);
                }
                return AvailabilityType.DOWN;
            }
        }
    }

    public OperationResult invokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("pause")) {
            endpoint.pause();
        } else if (name.equals("resume")) {
            endpoint.resume();
        }
        return null;
    }
}
