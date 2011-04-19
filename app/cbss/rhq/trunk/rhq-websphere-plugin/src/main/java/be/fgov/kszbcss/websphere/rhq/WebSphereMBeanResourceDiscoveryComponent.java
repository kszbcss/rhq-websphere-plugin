package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.EmsBeanName;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.plugins.jmx.JMXComponent;
import org.rhq.plugins.jmx.MBeanResourceDiscoveryComponent;
import org.rhq.plugins.jmx.ObjectNameQueryUtility;

public class WebSphereMBeanResourceDiscoveryComponent<T extends JMXComponent> extends MBeanResourceDiscoveryComponent<T> {
    private static final Log log = LogFactory.getLog(WebSphereMBeanResourceDiscoveryComponent.class);
    
    private ResourceDiscoveryContext<T> discoveryContext;
    
    @Override
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<T> context) {
        this.discoveryContext = context;
        // WebSphere MBean always have "unknown" properties; don't skip MBeans with these properties
        return discoverResources(context, false);
    }
    
    // There are two reasons why the MBean name is not usable as a resource key:
    //   1. It may easily be longer than 500 characters (which is the limit allowed by RHQ).
    //   2. For a given resource, the MBean name may change over time. E.g. the "version" key property
    //      will change after an upgrade.
    private String getResourceKey(EmsBeanName objectName) {
        String key = objectName.getKeyProperty("mbeanIdentifier");
        return key != null ? key : objectName.getCanonicalName();
    }
    
    // This is a copy & paste of the code in the super class (from version 3.0.0), except that we alter the resource key
    public Set<DiscoveredResourceDetails> performDiscovery(Configuration pluginConfiguration,
        JMXComponent parentResourceComponent, ResourceType resourceType, boolean skipUnknownProps) {

        String objectNameQueryTemplateOrig = pluginConfiguration.getSimple(PROPERTY_OBJECT_NAME).getStringValue();

        log.debug("Discovering MBean resources with object name query template: " + objectNameQueryTemplateOrig);

        EmsConnection connection = parentResourceComponent.getEmsConnection();

        if (connection == null) {
            throw new NullPointerException("The parent resource component [" + parentResourceComponent
                + "] returned a null connection - cannot discover MBeans without a connection");
        }

        Set<DiscoveredResourceDetails> services = new HashSet<DiscoveredResourceDetails>();
        String templates[] = objectNameQueryTemplateOrig.split("\\|");
        for (String objectNameQueryTemplate : templates) {
            // Get the query template, replacing the parent key variables with the values from the parent configuration
            ObjectNameQueryUtility queryUtility = new ObjectNameQueryUtility(objectNameQueryTemplate,
                (this.discoveryContext != null) ? this.discoveryContext.getParentResourceContext()
                    .getPluginConfiguration() : null);

            List<EmsBean> beans = connection.queryBeans(queryUtility.getTranslatedQuery());
            if (log.isDebugEnabled()) {
                log.debug("Found [" + beans.size() + "] mbeans for query [" + queryUtility.getTranslatedQuery() + "].");
            }
            for (EmsBean bean : beans) {
                if (queryUtility.setMatchedKeyValues(bean.getBeanName().getKeyProperties())) {
                    // Only use beans that have all the properties we've made variables of

                    // Don't match beans that have unexpected properties
                    if (skipUnknownProps
                        && queryUtility.isContainsExtraKeyProperties(bean.getBeanName().getKeyProperties().keySet())) {
                        continue;
                    }

                    String resourceKey = getResourceKey(bean.getBeanName()); // The detected object name

                    String nameTemplate = (pluginConfiguration.getSimple(PROPERTY_NAME_TEMPLATE) != null) ? pluginConfiguration
                        .getSimple(PROPERTY_NAME_TEMPLATE).getStringValue()
                        : null;

                    String descriptionTemplate = (pluginConfiguration.getSimple(PROPERTY_DESCRIPTION_TEMPLATE) != null) ? pluginConfiguration
                        .getSimple(PROPERTY_DESCRIPTION_TEMPLATE).getStringValue()
                        : null;

                    String name = resourceKey;
                    if (nameTemplate != null) {
                        name = queryUtility.formatMessage(nameTemplate);
                    }

                    String description = null;
                    if (descriptionTemplate != null) {
                        description = queryUtility.formatMessage(descriptionTemplate);
                    }

                    DiscoveredResourceDetails service = new DiscoveredResourceDetails(resourceType, resourceKey, name,
                        "", description, null, null);
                    Configuration config = service.getPluginConfiguration();
                    config.put(new PropertySimple(PROPERTY_OBJECT_NAME, bean.getBeanName().toString()));

                    Map<String, String> mappedVariableValues = queryUtility.getVariableValues();
                    for (String key : mappedVariableValues.keySet()) {
                        config.put(new PropertySimple(key, mappedVariableValues.get(key)));
                    }

                    services.add(service);

                    // Clear out the variables for the next bean detected
                    queryUtility.resetVariables();
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("[" + services.size() + "] services have been added");
            }

        }

        return services;
    }
}
