package be.fgov.kszbcss.rhq.websphere.config;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.exception.ConnectorException;

class RootPath extends Path {
    private static final Log log = LogFactory.getLog(RootPath.class);
    
    private final ConfigServiceWrapper configService;

    public RootPath(ConfigServiceWrapper configService) {
        this.configService = configService;
    }

    @Override
    ConfigObject[] resolveRelative(String relativePath) throws JMException, ConnectorException {
        if (relativePath == null) {
            throw new IllegalArgumentException("relativePath can't be null");
        }
        if (log.isDebugEnabled()) {
            log.debug("Resolving " + relativePath);
        }
        return configService.resolve(relativePath);
    }
    
}
