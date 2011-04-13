package be.fgov.kszbcss.websphere.rhq.ems.provider;

import java.util.Properties;

import javax.management.MBeanServer;

import org.mc4j.ems.impl.jmx.connection.support.providers.AbstractConnectionProvider;
import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.GenericMBeanServerProxy;

public class WebsphereConnectionProvider extends AbstractConnectionProvider {
    private GenericMBeanServerProxy statsProxy;
    private MBeanServer mbeanServer;

    protected void doConnect() throws Exception {
        Class<?> adminClientFactoryClass = WebsphereConnectionProvider.class.getClassLoader().loadClass("com.ibm.websphere.management.AdminClientFactory");

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

        Object adminClient = adminClientFactoryClass.getMethod("createAdminClient", Properties.class).invoke(null, properties);

        statsProxy = new GenericMBeanServerProxy(adminClient);
        mbeanServer = statsProxy.buildServerProxy();
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
