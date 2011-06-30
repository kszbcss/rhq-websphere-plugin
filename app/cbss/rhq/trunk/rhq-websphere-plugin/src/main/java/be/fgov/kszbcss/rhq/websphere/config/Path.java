package be.fgov.kszbcss.rhq.websphere.config;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.exception.ConnectorException;

public abstract class Path {
    private static final Log log = LogFactory.getLog(Path.class);
    
    abstract ConfigObject[] resolveRelative(String relativePath) throws JMException, ConnectorException;
    
    public final Path path(String type, String name) {
        return new RelativePath(this, type + "=" + name);
    }
    
    public final Path path(String type) {
        return path(type, "");
    }
    
    public ConfigObject[] resolve() throws JMException, ConnectorException {
        ConfigObject[] configObjects = resolveRelative(null);
        if (log.isDebugEnabled()) {
            if (configObjects.length == 0) {
                log.debug("No configuration data found");
            } else {
                StringBuilder buffer = new StringBuilder("Configuration data found:");
                for (ConfigObject configObject : configObjects) {
                    buffer.append("\n * ");
                    buffer.append(configObject.getId());
                }
                log.debug(buffer.toString());
            }
        }
        return configObjects;
    }
    
    public ConfigObject resolveSingle() throws JMException, ConnectorException {
        ConfigObject[] configObjects = resolve();
        if (configObjects.length == 1) {
            return configObjects[0];
        } else {
            // TODO: proper exception type
            throw new RuntimeException("More than one configuration object found");
        }
    }
}
