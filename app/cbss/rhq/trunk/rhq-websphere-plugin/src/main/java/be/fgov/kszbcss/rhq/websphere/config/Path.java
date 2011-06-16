package be.fgov.kszbcss.rhq.websphere.config;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.configservice.SystemAttributes;
import com.ibm.websphere.management.exception.ConnectorException;

public abstract class Path {
    private static final Log log = LogFactory.getLog(Path.class);
    
    abstract ObjectName[] resolveRelative(String relativePath) throws JMException, ConnectorException;
    
    public final Path path(String type, String name) {
        return new RelativePath(this, type + "=" + name);
    }
    
    public final Path path(String type) {
        return path(type, "");
    }
    
    public ObjectName[] resolve() throws JMException, ConnectorException {
        ObjectName[] names = resolveRelative(null);
        if (log.isDebugEnabled()) {
            if (names.length == 0) {
                log.debug("No configuration data found");
            } else {
                StringBuilder buffer = new StringBuilder("Configuration data found:");
                for (ObjectName name : names) {
                    buffer.append("\n * ");
                    buffer.append(name.getKeyProperty(SystemAttributes._WEBSPHERE_CONFIG_DATA_ID));
                }
                log.debug(buffer.toString());
            }
        }
        return names;
    }
    
    public ObjectName resolveSingle() throws JMException, ConnectorException {
        ObjectName[] objectNames = resolve();
        if (objectNames.length == 1) {
            return objectNames[0];
        } else {
            // TODO: proper exception type
            throw new RuntimeException("More than one configuration object found");
        }
    }
}
