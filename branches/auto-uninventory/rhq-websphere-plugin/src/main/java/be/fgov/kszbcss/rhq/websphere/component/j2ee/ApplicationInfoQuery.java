package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.management.exception.DocumentNotFoundException;

public class ApplicationInfoQuery implements ConfigQuery<ApplicationInfo> {
    private static final long serialVersionUID = 5507520583493264073L;
    
    private static final Log log = LogFactory.getLog(ApplicationInfoQuery.class);
    
    private final String applicationName;

    public ApplicationInfoQuery(String applicationName) {
        this.applicationName = applicationName;
    }

    public ApplicationInfo execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException {
        ConfigObject applicationDeployment = config.path("Deployment", applicationName).path("ApplicationDeployment").resolveSingle();
        String dataId = applicationDeployment.getId();
        String baseURI = dataId.substring(0, dataId.indexOf('|'));
        List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();
        for (ConfigObject module : applicationDeployment.getChildren("modules")) {
            String configDataType = module.getType();
            String uri = (String)module.getAttribute("uri");
            if (log.isDebugEnabled()) {
                log.debug("Processing module " + uri + ", type " + configDataType);
            }
            ModuleInfoFactory factory = ModuleInfoFactory.getInstance(configDataType);
            if (factory == null) {
                log.error("Unknown module type " + configDataType);
                continue;
            }
            String deploymentDescriptorURI = factory.locateDeploymentDescriptor(config, baseURI + "/" + uri);
            if (log.isDebugEnabled()) {
                log.debug("Loading deployment descriptor " + deploymentDescriptorURI);
            }
            moduleInfos.add(factory.create(uri, config.extract(deploymentDescriptorURI)));
        }
        byte[] deploymentDescriptor;
        try {
            deploymentDescriptor = config.extract(baseURI + "/META-INF/application.xml");
        } catch (JMException ex) {
            if (ex.getCause() instanceof DocumentNotFoundException) {
                deploymentDescriptor = null;
            } else {
                throw ex;
            }
        }
        return new ApplicationInfo(deploymentDescriptor, moduleInfos.toArray(new ModuleInfo[moduleInfos.size()]));
    }

    @Override
    public int hashCode() {
        return applicationName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApplicationInfoQuery) {
            ApplicationInfoQuery other = (ApplicationInfoQuery)obj;
            return other.applicationName.equals(applicationName);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + applicationName + ")";
    }
}
