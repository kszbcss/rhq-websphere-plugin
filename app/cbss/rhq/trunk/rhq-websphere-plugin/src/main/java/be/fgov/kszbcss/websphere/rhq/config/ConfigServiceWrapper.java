package be.fgov.kszbcss.websphere.rhq.config;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.websphere.rhq.mbean.MBeanClient;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.exception.ConnectorException;

public class ConfigServiceWrapper {
    private static final Log log = LogFactory.getLog(ConfigServiceWrapper.class);
    
    private final MBeanClient configServiceMBeanClient;
    private final ConfigService configService;
    private final ConfigRepository configRepository;
    private final Session session;
    
    ConfigServiceWrapper(MBeanClient configServiceMBeanClient, ConfigRepository configRepository, Session session) {
        // TODO: do this in a smarter way; we should be able to get back from the proxy to the MBeanClient
        this.configServiceMBeanClient = configServiceMBeanClient;
        this.configService = configServiceMBeanClient.getProxy(ConfigService.class);
        this.configRepository = configRepository;
        this.session = session;
    }
    
    /**
     * Get the WebSphere version. The returned value is actually the version number of the deployment
     * manager (which may differ from the version number of the application servers).
     * 
     * @return
     * @throws JMException
     * @throws ConnectorException
     */
    public String getWebSphereVersion() throws JMException, ConnectorException {
        return configServiceMBeanClient.getObjectName(false).getKeyProperty("version");
    }
    
    public ObjectName[] resolve(String containmentPath) throws JMException, ConnectorException {
        return configService.resolve(session, containmentPath);
    }
    
    public Object getAttribute(ObjectName parent, String attributeName) throws JMException, ConnectorException {
        return configService.getAttribute(session, parent, attributeName);
    }
    
    public String[] listResourceNames(String parent, int type, int depth) throws JMException, ConnectorException {
        return configRepository.listResourceNames(parent, type, depth);
    }

    public byte[] extract(String docURI) throws JMException, ConnectorException {
        return configRepository.extract(docURI);
    }
    
    void destroy() {
        try {
            configService.discard(session);
        } catch (Exception ex) {
            log.warn("Unexpected exception when discarding workspace " + session, ex);
        }
    }
}
