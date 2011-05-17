package be.fgov.kszbcss.websphere.rhq.support.configuration;

import java.util.HashSet;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionSimple;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;

import be.fgov.kszbcss.websphere.rhq.MBean;
import be.fgov.kszbcss.websphere.rhq.WebSphereComponent;

public class ConfigurationFacetSupport implements ConfigurationFacet {
    private final WebSphereComponent<?> component;
    private final MBean mbean;
    
    public ConfigurationFacetSupport(WebSphereComponent<?> component, MBean mbean) {
        this.component = component;
        this.mbean = mbean;
    }

    public Configuration loadResourceConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        ConfigurationDefinition configurationDefinition = component.getResourceContext().getResourceType().getResourceConfigurationDefinition();
        Set<String> attributeNames = new HashSet<String>();
        for (PropertyDefinition property : configurationDefinition.getPropertyDefinitions().values()) {
            if (property instanceof PropertyDefinitionSimple) {
                attributeNames.add(property.getName());
            }
        }
        AttributeList attributes = mbean.getAttributes(attributeNames.toArray(new String[attributeNames.size()]));
        for (int i=0; i<attributes.size(); i++) {
            Attribute attribute = (Attribute)attributes.get(i);
            configuration.put(new PropertySimple(attribute.getName(), attribute.getValue()));
        }
        return configuration;
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
        // TODO
        throw new UnsupportedOperationException();
    }
}
