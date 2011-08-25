package be.fgov.kszbcss.rhq.websphere.support.configuration;

import java.util.HashSet;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionSimple;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.util.exception.ThrowableUtil;

import be.fgov.kszbcss.rhq.websphere.component.WebSphereComponent;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;

public class ConfigurationFacetSupport implements ConfigurationFacet {
    private final WebSphereComponent<?> component;
    private final MBeanClient mbean;
    
    public ConfigurationFacetSupport(WebSphereComponent<?> component, MBeanClient mbean) {
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
        ConfigurationDefinition configurationDefinition = component.getResourceContext().getResourceType().getResourceConfigurationDefinition();
        AttributeList attributes = new AttributeList();
        for (PropertySimple property : report.getConfiguration().getSimpleProperties().values()) {
            PropertyDefinitionSimple definition = configurationDefinition.getPropertyDefinitionSimple(property.getName());
            if (!definition.isReadOnly()) {
                Object value;
                switch (definition.getType()) {
                    case INTEGER:
                        value = Integer.valueOf(property.getIntegerValue());
                        break;
                    case LONG:
                        value = Long.valueOf(property.getLongValue());
                        break;
                    case BOOLEAN:
                        value = Boolean.valueOf(property.getBooleanValue());
                        break;
                    case FLOAT:
                        value = Float.valueOf(property.getFloatValue());
                        break;
                    case DOUBLE:
                        value = Double.valueOf(property.getDoubleValue());
                        break;
                    default:
                        value = property.getStringValue();
                        break;
                }
                attributes.add(new Attribute(property.getName(), value));
            }
        }
        try {
            mbean.setAttributes(attributes);
            report.setStatus(ConfigurationUpdateStatus.SUCCESS);
        } catch (Exception ex) {
            report.setErrorMessage(ThrowableUtil.getStackAsString(ex));
            report.setStatus(ConfigurationUpdateStatus.FAILURE);
        }
    }
}
