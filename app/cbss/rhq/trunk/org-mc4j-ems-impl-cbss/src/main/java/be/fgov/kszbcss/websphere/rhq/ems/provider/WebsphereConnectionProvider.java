package be.fgov.kszbcss.websphere.rhq.ems.provider;

import java.util.Properties;

import javax.management.MBeanServer;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.impl.jmx.connection.support.providers.AbstractConnectionProvider;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.security.auth.WSSubject;

public class WebsphereConnectionProvider extends AbstractConnectionProvider {
    private static final Log log = LogFactory.getLog(WebsphereConnectionProvider.class);
    
    private AdminClientProxy proxy;
    private MBeanServer mbeanServer;

    protected void doConnect() throws Exception {
        ClassLoader cl = WebsphereConnectionProvider.class.getClassLoader();
        
        // Setting the context class loader is required so that the AdminClientFactory can find
        // the initial context factory
        Thread thread = Thread.currentThread();
        ClassLoader savedTCCL = thread.getContextClassLoader();
        thread.setContextClassLoader(cl);
        try {
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
            
            properties.put(AdminClient.CONNECTOR_TYPE, AdminClient.CONNECTOR_TYPE_RMI);
            properties.setProperty(AdminClient.CONNECTOR_HOST, host);
            properties.setProperty(AdminClient.CONNECTOR_PORT, port);
    
            String principal = connectionSettings.getPrincipal(); 
            if (principal != null && principal.length() > 0) { 
                properties.setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED, "true"); 
                properties.setProperty(AdminClient.USERNAME, principal); 
                properties.setProperty(AdminClient.PASSWORD, connectionSettings.getCredentials()); 
            } else { 
                properties.setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED, "false");
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Creating AdminClient with properties: " + properties);
            }
            
            AdminClient adminClient = AdminClientFactory.createAdminClient(properties);
            Subject subject = WSSubject.getRunAsSubject();
            if (log.isDebugEnabled()) {
                log.debug("Subject = " + subject);
            }
            WSSubject.setRunAsSubject(null);
    
            proxy = new AdminClientProxy(adminClient, subject);
            mbeanServer = proxy.buildServerProxy();
        } finally {
            thread.setContextClassLoader(savedTCCL);
        }
    }

    public MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    public long getRoundTrips() {
        return proxy.getRoundTrips();
    }

    public long getFailures() {
        return proxy.getFailures();
    }
}
