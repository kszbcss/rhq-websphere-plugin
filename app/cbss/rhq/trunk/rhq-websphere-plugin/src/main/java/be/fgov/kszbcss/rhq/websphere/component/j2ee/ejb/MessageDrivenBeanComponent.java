package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.SIBDestination;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.SIBDestinationMap;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.SIBDestinationMapQuery;
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

    @Override
    public void loadResourceConfiguration(Configuration configuration) throws Exception {
        super.loadResourceConfiguration(configuration);
        ManagedServer server = getServer();
        List<Map<String,String>> data = getModule().getApplication().getConfiguration().getData("BindJndiForEJBMessageBinding", getModuleName(), getBeanName());
        if (data == null || data.size() != 1) {
            throw new Exception("No message listener binding found");
        }
        Map<String,String> binding = data.get(0);
        String activationSpecJndiName = binding.get("JNDI");
        String destinationJndiName = binding.get("jndi.dest");
        if (destinationJndiName == null) {
            // In this case, the destination is specified in the activation spec
            ActivationSpecInfo activationSpec = server.queryConfig(new ActivationSpecQuery(server.getNode(), server.getServer())).getActivationSpec(activationSpecJndiName);
            if (activationSpec != null) {
                destinationJndiName = activationSpec.getDestinationJndiName();
            }
        }
        configuration.put(new PropertySimple("activationSpecJndiName", activationSpecJndiName));
        configuration.put(new PropertySimple("destinationJndiName", destinationJndiName));
        if (destinationJndiName != null) {
            SIBDestinationMap sibDestinationMap = server.queryConfig(new SIBDestinationMapQuery(server.getNode(), server.getServer()));
            SIBDestination dest = sibDestinationMap.getSIBDestination(destinationJndiName);
            if (dest != null) {
                configuration.put(new PropertySimple("busName", dest.getBusName()));
                configuration.put(new PropertySimple("destinationName", dest.getDestinationName()));
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
