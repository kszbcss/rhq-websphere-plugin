package be.fgov.kszbcss.rhq.websphere;

import java.util.ArrayList;
import java.util.List;

import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigServiceWrapper;

import com.ibm.websphere.management.configservice.ConfigServiceHelper;
import com.ibm.websphere.management.configservice.SystemAttributes;
import com.ibm.websphere.management.exception.ConnectorException;

public class ApplicationInfoQuery implements ConfigQuery<ApplicationInfo> {
    private static final long serialVersionUID = 5507520583493264072L;
    
    private static final Log log = LogFactory.getLog(ApplicationInfoQuery.class);
    
    private final String applicationName;

    public ApplicationInfoQuery(String applicationName) {
        this.applicationName = applicationName;
    }

    public ApplicationInfo execute(ConfigServiceWrapper configService) throws JMException, ConnectorException {
        // AdminConfig.getid("/Deployment:TUMEnterprise/ApplicationDeployment:/")
        
        // TODO: clean this up:
        ObjectName applicationDeployment = configService.resolve("Deployment=" + applicationName + ":ApplicationDeployment=")[0];
        String dataId = applicationDeployment.getKeyProperty(SystemAttributes._WEBSPHERE_CONFIG_DATA_ID);
        String baseURI = dataId.substring(0, dataId.indexOf('|'));
        List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();
        for (AttributeList module : (List<AttributeList>)configService.getAttribute(applicationDeployment, "modules")) {
            String configDataType = (String)ConfigServiceHelper.getAttributeValue(module, SystemAttributes._WEBSPHERE_CONFIG_DATA_TYPE);
            String uri = (String)ConfigServiceHelper.getAttributeValue(module, "uri");
            if (log.isDebugEnabled()) {
                log.debug("Processing module " + uri + ", type " + configDataType);
            }
            ModuleInfoFactory factory = ModuleInfoFactory.getInstance(configDataType);
            if (factory == null) {
                log.error("Unknown module type " + configDataType);
                continue;
            }
            String deploymentDescriptorURI = factory.locateDeploymentDescriptor(configService, baseURI + "/" + uri);
            if (log.isDebugEnabled()) {
                log.debug("Loading deployment descriptor " + deploymentDescriptorURI);
            }
            moduleInfos.add(factory.create(uri, configService.extract(deploymentDescriptorURI)));
        }
        return new ApplicationInfo(moduleInfos.toArray(new ModuleInfo[moduleInfos.size()]));
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
