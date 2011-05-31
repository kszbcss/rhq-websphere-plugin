package be.fgov.kszbcss.websphere.rhq.mbean;

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.websphere.rhq.WebSphereServer;

public class MBeanClientFactory {
    private static final Log log = LogFactory.getLog(MBeanClientFactory.class);
    
    private final ProcessInfo processInfo;
    private final Map<MBeanLocator,MBeanClient> cache = new HashMap<MBeanLocator,MBeanClient>();
    
    public MBeanClientFactory(WebSphereServer server) {
        processInfo = new ProcessInfo(server);
    }
    
    /**
     * Get a client for the MBean identified by the given locator. This method may either return a
     * cached instance or create a new one. Note that since the MBean is located lazily (during the
     * first invocation), this method will never fail, even if the connection to the server is
     * unavailable or if no matching MBean is registered.
     * 
     * @param locator
     *            the locator identifying the MBean
     * @return the client instance
     */
    public MBeanClient getMBeanClient(MBeanLocator locator) {
        synchronized (cache) {
            MBeanClient client = cache.get(locator);
            if (client == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating new MBeanClient for locator: " + locator);
                }
                client = new MBeanClient(processInfo, locator);
                cache.put(locator, client);
            }
            return client;
        }
    }

    public MBeanClient getMBeanClient(ObjectName objectNamePattern) {
        return getMBeanClient(new MBeanObjectNamePatternLocator(objectNamePattern));
    }
    
    public MBeanClient getMBeanClient(String objectNamePattern) {
        try {
            return getMBeanClient(new ObjectName(objectNamePattern));
        } catch (MalformedObjectNameException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
