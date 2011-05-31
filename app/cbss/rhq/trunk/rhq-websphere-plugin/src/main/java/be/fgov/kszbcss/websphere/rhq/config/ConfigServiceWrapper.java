package be.fgov.kszbcss.websphere.rhq.config;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.exception.ConnectorException;

public class ConfigServiceWrapper {
    private static final Log log = LogFactory.getLog(ConfigServiceWrapper.class);
    
    private final ConfigService configService;
    private final Session session;
    
    ConfigServiceWrapper(ConfigService configService, Session session) {
        this.configService = configService;
        this.session = session;
    }
    
    public ObjectName[] resolve(String containmentPath) throws JMException, ConnectorException {
        return configService.resolve(session, containmentPath);
    }
    
    public Object getAttribute(ObjectName parent, String attributeName) throws JMException, ConnectorException {
        return configService.getAttribute(session, parent, attributeName);
    }
    
    void destroy() {
        try {
            configService.discard(session);
        } catch (Exception ex) {
            log.warn("Unexpected exception when discarding workspace " + session, ex);
        }
    }
}
