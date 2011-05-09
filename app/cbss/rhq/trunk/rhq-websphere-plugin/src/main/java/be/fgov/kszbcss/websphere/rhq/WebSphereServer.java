package be.fgov.kszbcss.websphere.rhq;

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import javax.management.JMException;
import javax.management.ObjectName;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.EmsException;
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.domain.configuration.Configuration;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.StatDescriptor;
import com.ibm.websphere.pmi.stat.WSStats;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;

public class WebSphereServer {
    private static final Log log = LogFactory.getLog(WebSphereServer.class);
    
    private final Configuration config;
    private AdminClient adminClient;
    private ObjectName perfMBean;
    
    public WebSphereServer(Configuration config) {
        this.config = config;
    }
    
    public synchronized AdminClient getAdminClient() throws ConnectorException {
        if (adminClient == null) {
            Properties properties = new Properties();
            
            properties.put(AdminClient.CONNECTOR_TYPE, AdminClient.CONNECTOR_TYPE_RMI);
            properties.setProperty(AdminClient.CONNECTOR_HOST, config.getSimpleValue("host", null));
            properties.setProperty(AdminClient.CONNECTOR_PORT, config.getSimpleValue("port", null));

            String principal = config.getSimpleValue("principal", null); 
            if (principal != null && principal.length() > 0) { 
                properties.setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED, "true"); 
                properties.setProperty(AdminClient.USERNAME, principal); 
                properties.setProperty(AdminClient.PASSWORD, config.getSimpleValue("credentials", null)); 
            } else { 
                properties.setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED, "false");
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Creating AdminClient with properties: " + properties);
            }
            
            adminClient = AdminClientFactory.createAdminClient(properties);
            try {
                Subject subject = WSSubject.getRunAsSubject();
                if (log.isDebugEnabled()) {
                    log.debug("Subject = " + subject);
                }
                if (subject != null) {
                    WSSubject.setRunAsSubject(null);
                    adminClient = new SecureAdminClient(adminClient, subject);
                }
            } catch (WSSecurityException ex) {
                throw new ConnectorException(ex);
            }
        }
        return adminClient;
    }
    
    private synchronized ObjectName getPerfMBean() throws JMException, ConnectorException {
        if (perfMBean == null) {
            Set<ObjectName> names = getAdminClient().queryNames(new ObjectName("WebSphere:type=Perf,*"), null);
            // TODO: check that there is exactly one result
            perfMBean = names.iterator().next();
        }
        return perfMBean;
    }
    
    public WSStats getWSStats(MBeanStatDescriptor descriptor) throws JMException, ConnectorException {
        WSStats stats = (WSStats)getAdminClient().invoke(getPerfMBean(), "getStatsObject",
                new Object[] { descriptor, Boolean.TRUE },
                new String[] { MBeanStatDescriptor.class.getName(), Boolean.class.getName() });
        if (log.isDebugEnabled()) {
            if (stats == null) {
                log.debug("No stats found for " + descriptor);
            } else {
                log.debug("Loaded statistics for " + descriptor
                        + ":\n  Stats type: " + stats.getClass().getName()
                        + "\n  Available statistics: " + Arrays.asList(stats.getStatisticNames()));
            }
        }
        return stats;
    }
    
    public WSStats getWSStats(EmsBean bean, String... path) {
        if (bean == null) {
            throw new IllegalArgumentException("getWSStats: bean can't be null");
        }
        try {
            ObjectName mbean = new ObjectName(bean.getBeanName().toString());
            return getWSStats(path.length == 0 ? new MBeanStatDescriptor(mbean) : new MBeanStatDescriptor(mbean, new StatDescriptor(path)));
        } catch (JMException ex) {
            throw new EmsException(ex);
        } catch (ConnectorException ex) {
            throw new EmsException(ex);
        }
    }
}
