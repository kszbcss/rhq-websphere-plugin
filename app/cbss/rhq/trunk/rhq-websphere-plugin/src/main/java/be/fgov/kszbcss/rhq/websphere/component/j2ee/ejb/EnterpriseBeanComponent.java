package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.SIBDestination;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.SIBDestinationMap;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.SIBDestinationMapQuery;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;

import com.ibm.websphere.pmi.PmiConstants;

public abstract class EnterpriseBeanComponent extends WebSphereServiceComponent<EJBModuleComponent> implements MeasurementFacet, ConfigurationFacet {
    private static final Log log = LogFactory.getLog(EnterpriseBeanComponent.class);
    
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<EJBModuleComponent> context = getResourceContext();
        ModuleComponent parent = context.getParentResourceComponent();
        WebSphereServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(this);
//        MBeanClient mbean = server.getMBeanClient("WebSphere:type=" + getMBeanType() + ",Application=" + parent.getApplicationName() + ",EJBModule=" + parent.getModuleName() + ",name=" + context.getResourceKey() + ",*");
        // Applications may be installed with "Create MBeans for resources" disabled. In this case, there
        // is no MBean representing the bean. Therefore we always locate the PMI module starting from the
        // server.
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(),
                PmiConstants.BEAN_MODULE, parent.getApplicationName() + "#" + parent.getModuleName(),
                getPMISubmodule(), context.getResourceKey()));
        context.getParentResourceComponent().registerLogEventContext(context.getResourceKey(), context.getEventContext());
    }
    
    protected abstract EnterpriseBeanType getType();
    
    // TODO: check if this is still needed
    protected abstract String getMBeanType();
    
    protected abstract String getPMISubmodule();
    
    public String getBeanName() {
        return getResourceContext().getResourceKey();
    }
    
    public EJBModuleComponent getModule() {
        return getResourceContext().getParentResourceComponent();
    }
    
    public String getModuleName() {
        return getModule().getModuleName();
    }
    
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public AvailabilityType getAvailability() {
        // Same as for servlets: we check that the bean is still present in the deployment
        // descriptor.
        try {
            return getModule().getBeanNames(getType()).contains(getBeanName()) ? AvailabilityType.UP : AvailabilityType.DOWN;
        } catch (Exception ex) {
            return AvailabilityType.DOWN;
        }
    }

    public Configuration loadResourceConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        List<Map<String,String>> data = getModule().getApplication().getConfiguration().getData("MapMessageDestinationRefToEJB", getModuleName(), getBeanName());
        if (data != null && data.size() != 0) {
            ManagedServer server = getServer();
            SIBDestinationMap sibDestinationMap = server.queryConfig(new SIBDestinationMapQuery(server.getNode(), server.getServer()));
            PropertyList list = new PropertyList("messagingDestinationRefs");
            for (Map<String,String> entry : data) {
                PropertyMap map = new PropertyMap("messagingDestinationRef");
                String refName = entry.get("messageDestinationRefName");
                String jndiName = entry.get("JNDI");
                map.put(new PropertySimple("name", refName));
                map.put(new PropertySimple("bindingName", jndiName));
                SIBDestination dest = sibDestinationMap.getSIBDestination(jndiName);
                if (dest != null) {
                    map.put(new PropertySimple("busName", dest.getBusName()));
                    map.put(new PropertySimple("destinationName", dest.getDestinationName()));
                }
                list.add(map);
                if (log.isDebugEnabled()) {
                    log.debug("Discovered binding: " + map);
                }
            }
            configuration.put(list);
        }
        return configuration;
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport arg0) {
    }

    public void stop() {
        ResourceContext<EJBModuleComponent> context = getResourceContext();
        context.getParentResourceComponent().unregisterLogEventContext(context.getResourceKey());
    }
}
