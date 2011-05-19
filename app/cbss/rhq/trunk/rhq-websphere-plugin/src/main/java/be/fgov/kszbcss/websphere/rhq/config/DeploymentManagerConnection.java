package be.fgov.kszbcss.websphere.rhq.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.websphere.rhq.Utils;
import be.fgov.kszbcss.websphere.rhq.WebSphereServer;
import be.fgov.kszbcss.websphere.rhq.mbean.MBean;

import com.ibm.websphere.management.configservice.ConfigService;
import com.ibm.websphere.management.configservice.ConfigServiceProxy;
import com.ibm.websphere.management.repository.ConfigEpoch;

/**
 * Manages the communication with the deployment manager of a given WebSphere cell. There will be
 * one instance of this class for each cell for which there is at least one monitored WebSphere
 * instance.
 */
class DeploymentManagerConnection implements Runnable {
    private static final Log log = LogFactory.getLog(DeploymentManagerConnection.class);

    private final MBean configRepositoryMBean;
    private ConfigEpoch epoch;
    private ConfigService configService;
    
    DeploymentManagerConnection(WebSphereServer server) {
        // TODO: maybe we should handle the routing stuff somewhere else?
        configRepositoryMBean = new MBean(server, Utils.createObjectName("WebSphere:type=ConfigRepository,cell=" + server.getCell() + ",node=" + server.getNode() + ",process=" + server.getServer() + ",*"));
    }
    
    public void run() {
        ConfigEpoch epoch = null;
        Exception exception = null;
        try {
            epoch = (ConfigEpoch)configRepositoryMBean.invoke("getRepositoryEpoch", new Object[0], new String[0]);
        } catch (Exception ex) {
            exception = ex;
        }
        synchronized (this) {
            if (this.epoch != null && exception != null) {
                log.error("Lost connection to the deployment manager", exception);
            } else if (this.epoch == null && exception == null) {
                log.info("Connection to deployment manager reestablished");
            }
            this.epoch = epoch;
        }
    }
    
    synchronized ConfigEpoch getEpoch() {
        return epoch;
    }
    
    synchronized ConfigService getConfigService() {
        // TODO
        return null;
    }

    void decrementRefCount() {
        
    }
}
