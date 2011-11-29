package be.fgov.kszbcss.rhq.websphere.component.j2ee.web;

import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.w3c.dom.Element;

import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.DeploymentConfigurationFacetSupport;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;

import com.ibm.websphere.management.exception.ConnectorException;

public class WebModuleComponent extends ModuleComponent implements ConfigurationFacet {
    private DeploymentConfigurationFacetSupport configurationFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        super.start();
        configurationFacetSupport = new DeploymentConfigurationFacetSupport(getApplication(), getModuleName(), null);
    }

    @Override
    protected String getMBeanType() {
        return "WebModule";
    }
    
    public Set<String> getServletNames() throws JMException, ConnectorException {
        Set<String> result = new HashSet<String>();
        for (Element servlet : Utils.getElements(getModuleInfo().getDeploymentDescriptor().getDocumentElement(), "servlet")) {
            result.add(Utils.getFirstElement(servlet, "servlet-name").getTextContent());
        }
        return result;
    }

    public Configuration loadResourceConfiguration() throws Exception {
        return configurationFacetSupport.loadResourceConfiguration();
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
    }
}
