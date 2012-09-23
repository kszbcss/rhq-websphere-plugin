package be.fgov.kszbcss.rhq.websphere;

import java.util.Properties;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.connector.AdminClientProvider;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.exception.ConnectorException;

public abstract class ProcessLocator implements AdminClientProvider {
    private static final Log log = LogFactory.getLog(ProcessLocator.class);
    
    public final AdminClient createAdminClient() throws ConnectorException {
        Properties properties = new Properties();
        
        try {
            getAdminClientProperties(properties);
        } catch (JMException ex) {
            // TODO: define a proper exception for this
            throw new ConnectorException(ex);
        }
        
        // From the IBM site: "When you use the createAdminClient method within application code that
        // runs on an application server, such as within servlets and JavaServer Pages (JSP) files,
        // you must set the CACHE_DISABLED property to true." Since we use multiple threads and access
        // multiple servers, we assume that this also applies to us.
        properties.setProperty(AdminClient.CACHE_DISABLED, "true");
    
        if (log.isDebugEnabled()) {
            log.debug("Creating AdminClient with properties: " + properties);
        }
        
        return AdminClientFactory.createAdminClient(properties);
    }

    public abstract void getAdminClientProperties(Properties properties) throws JMException, ConnectorException;
}
