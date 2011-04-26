package be.fgov.kszbcss.websphere.rhq.ems.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.management.MBeanServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.impl.jmx.connection.support.providers.AbstractConnectionProvider;
import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.GenericMBeanServerProxy;

public class WebsphereConnectionProvider extends AbstractConnectionProvider {
    private static final Log log = LogFactory.getLog(WebsphereConnectionProvider.class);
    
    private GenericMBeanServerProxy statsProxy;
    private MBeanServer mbeanServer;

    protected void doConnect() throws Exception {
        ClassLoader cl = WebsphereConnectionProvider.class.getClassLoader();
        
        // Setting the context class loader is required so that the AdminClientFactory can find
        // the initial context factory
        Thread thread = Thread.currentThread();
        ClassLoader savedTCCL = thread.getContextClassLoader();
        thread.setContextClassLoader(cl);
        try {
            Class<?> adminClientFactoryClass = cl.loadClass("com.ibm.websphere.management.AdminClientFactory");
    
            Properties properties = new Properties();
            
            String serverURL = connectionSettings.getServerUrl();
            
            String host;
            String port;
            int idx = serverURL.indexOf(':');
            if (idx == -1) {
                host = serverURL;
                port = "9100";
            } else {
                host = serverURL.substring(0, idx);
                port = serverURL.substring(idx+1);
            }
            
            properties.put("type", "RMI");
            properties.setProperty("host", host);
            properties.setProperty("port", port);
    
            String principal = connectionSettings.getPrincipal(); 
            if (principal != null && principal.length() > 0) { 
                properties.setProperty("securityEnabled", "true"); 
                properties.setProperty("username", principal); 
                properties.setProperty("password", connectionSettings.getCredentials()); 
            } else { 
                properties.setProperty("securityEnabled", "false");
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Creating AdminClient with properties: " + properties);
            }
            
            Object adminClient;
            try {
                adminClient = adminClientFactoryClass.getMethod("createAdminClient", Properties.class).invoke(null, properties);
            } catch (InvocationTargetException ex) {
                // Unwrap the exception to avoid lengthy stack traces
                Throwable cause = ex.getCause();
                if (cause instanceof Exception) {
                    throw (Exception)cause;
                } else {
                    throw ex;
                }
            }
    
            statsProxy = new GenericMBeanServerProxy(adminClient);
            mbeanServer = statsProxy.buildServerProxy();
        } finally {
            thread.setContextClassLoader(savedTCCL);
        }
    }

    public MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    public long getRoundTrips() {
        return statsProxy.getRoundTrips();
    }

    public long getFailures() {
        return statsProxy.getFailures();
    }
}
