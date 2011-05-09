package be.fgov.kszbcss.websphere.rhq;

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import javax.management.JMException;
import javax.management.ObjectName;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiModuleConfig;
import com.ibm.websphere.pmi.stat.MBeanLevelSpec;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.WSStats;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;

public class WebSphereServer {
    private static final Log log = LogFactory.getLog(WebSphereServer.class);
    
    private final Configuration config;
    private AdminClient adminClient;
    private ObjectName perfMBean;
    private PmiModuleConfig[] pmiModuleConfigs;
    
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
                        + ":\n  WSStats class: " + stats.getClass().getName()
                        + "\n  Stats type: " + stats.getStatsType()
                        + "\n  Available statistics: " + Arrays.asList(stats.getStatisticNames()));
            }
        }
        return stats;
    }
    
    private synchronized PmiModuleConfig[] getPmiModuleConfigs() throws JMException, ConnectorException {
        if (pmiModuleConfigs == null) {
            pmiModuleConfigs = (PmiModuleConfig[])getAdminClient().invoke(getPerfMBean(), "getConfigs", new Object[0], new String[0]);
        }
        return pmiModuleConfigs;
    }
    
    public PmiModuleConfig getPmiModuleConfig(WSStats stats) throws JMException, ConnectorException {
        String statsType = stats.getStatsType();
        int dashIndex = statsType.indexOf('#');
        if (dashIndex != -1) {
            statsType = statsType.substring(0, dashIndex);
        }
        for (PmiModuleConfig config : getPmiModuleConfigs()) {
            if (config.getUID().equals(statsType)) {
                return config;
            }
        }
        log.error("Unable to locate PMI module config for " + statsType);
        return null;
    }
    
    public void enableStatistics(MBeanStatDescriptor descriptor, Set<Integer> statisticsToEnable) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to enable statistics " + statisticsToEnable + " on " + descriptor);
            }
            AdminClient adminClient = getAdminClient();
            ObjectName perfMBean = getPerfMBean();
            
            // Load the existing instrumentation level
            MBeanLevelSpec[] specs = (MBeanLevelSpec[])adminClient.invoke(perfMBean, "getInstrumentationLevel",
                    new Object[] { descriptor, Boolean.FALSE },
                    new String[] { MBeanStatDescriptor.class.getName(), Boolean.class.getName() });
            if (specs.length != 1) {
                log.error("Expected getInstrumentationLevel to return exactly one MBeanLevelSpec object");
                return;
            }
            MBeanLevelSpec spec = specs[0];
            if (log.isDebugEnabled()) {
                log.debug("Current instrumentation level: " + spec);
            }
            
            // Add the IDs of the statistics to be enabled
            int[] oldEnabled = spec.getEnabled();
            int[] newEnabled = new int[oldEnabled.length+statisticsToEnable.size()];
            System.arraycopy(oldEnabled, 0, newEnabled, 0, oldEnabled.length);
            int index = oldEnabled.length;
            for (Integer dataId : statisticsToEnable) {
                newEnabled[index++] = dataId;
            }
            spec.setEnabled(newEnabled);
            if (log.isDebugEnabled()) {
                log.debug("New instrumentation level: " + spec);
            }
            
            // Now update the instrumentation level
            adminClient.invoke(perfMBean, "setInstrumentationLevel",
                    new Object[] { spec, Boolean.FALSE },
                    new String[] { MBeanLevelSpec.class.getName(), Boolean.class.getName() });
            
            log.info("Enabled statistics " + statisticsToEnable + " on " + descriptor);
        } catch (Exception ex) {
            log.error(ex); // TODO
        }
    }
}
