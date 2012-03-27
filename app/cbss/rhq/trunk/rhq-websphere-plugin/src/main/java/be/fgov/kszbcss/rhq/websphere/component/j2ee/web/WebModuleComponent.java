package be.fgov.kszbcss.rhq.websphere.component.j2ee.web;

import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.w3c.dom.Element;

import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ApplicationComponent;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.DeploymentConfigurationFacetSupport;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;

import com.ibm.websphere.management.exception.ConnectorException;

public class WebModuleComponent extends ModuleComponent implements ConfigurationFacet {
    private DeploymentConfigurationFacetSupport configurationFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        super.start();
        configurationFacetSupport = new DeploymentConfigurationFacetSupport(getApplication(), getModuleName(), null);
        ResourceContext<ApplicationComponent> context = getResourceContext();
        PropertySimple suppressLogEventsProp = context.getPluginConfiguration().getSimple("suppressLogEvents");
        boolean suppressLogEvents = suppressLogEventsProp != null && Boolean.TRUE.equals(suppressLogEventsProp.getBooleanValue());
        getApplication().registerLogEventContext(getModuleName(), suppressLogEvents ? null : context.getEventContext());
    }

    @Override
    protected String getMBeanType() {
        return "WebModule";
    }
    
    public Set<String> getServletNames() throws JMException, ConnectorException, InterruptedException {
        Set<String> result = new HashSet<String>();
        for (Element servlet : Utils.getElements(getModuleInfo().getDeploymentDescriptor().getDocumentElement(), "servlet")) {
            result.add(Utils.getFirstElement(servlet, "servlet-name").getTextContent());
        }
        return result;
    }

    public Configuration loadResourceConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        configurationFacetSupport.loadResourceConfiguration(configuration);
        return configuration;
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
    }

    @Override
    public void stop() {
        getApplication().unregisterLogEventContext(getModuleName());
        super.stop();
    }
}
