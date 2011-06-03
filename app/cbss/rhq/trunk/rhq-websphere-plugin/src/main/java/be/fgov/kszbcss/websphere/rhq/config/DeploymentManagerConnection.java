package be.fgov.kszbcss.websphere.rhq.config;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.websphere.rhq.DeploymentManager;

import com.ibm.websphere.management.repository.ConfigEpoch;

/**
 * Manages the communication with the deployment manager of a given WebSphere cell. There will be
 * one instance of this class for each cell for which there is at least one monitored WebSphere
 * instance.
 */
class DeploymentManagerConnection implements Runnable {
    private static final Log log = LogFactory.getLog(DeploymentManagerConnection.class);

    private final ConfigQueryServiceFactory factory;
    private final ConfigRepository configRepository;
    private final ConfigServiceWrapper configService;
    private final ScheduledFuture<?> future;
    private ConfigEpoch epoch;
    private int refCount;
    private boolean polled;
    
    DeploymentManagerConnection(ConfigQueryServiceFactory factory, DeploymentManager dm, ScheduledExecutorService executorService) {
        this.factory = factory;
        configRepository = dm.getMBeanClient("WebSphere:type=ConfigRepository,*").getProxy(ConfigRepository.class);
        configService = new ConfigServiceWrapper(dm.getMBeanClient("WebSphere:type=ConfigService,*"), configRepository);
        future = executorService.scheduleWithFixedDelay(this, 0, 30, TimeUnit.SECONDS);
    }
    
    public void run() {
        ConfigEpoch epoch = null;
        Exception exception = null;
        try {
            epoch = configRepository.getRepositoryEpoch();
        } catch (Exception ex) {
            exception = ex;
        }
        synchronized (this) {
            if (this.epoch != null && exception != null) {
                log.error("Lost connection to the deployment manager", exception);
            } else if (!polled && exception != null) {
                log.error("Connection to deployment manager unavailable", exception);
            } else if (this.epoch == null && exception == null) {
                if (polled) {
                    log.info("Connection to deployment manager reestablished");
                } else {
                    log.info("Connection to deployment manager established");
                }
            } else if (this.epoch != null && epoch != null && !this.epoch.equals(epoch)) {
                if (log.isDebugEnabled()) {
                    log.debug("Epoch change detected; old epoch: " + this.epoch + "; new epoch: " + epoch);
                }
            }
            if (epoch != null && !epoch.equals(this.epoch)) {
                // The ConfigService actually creates a workspace on the deployment manager. This workspace
                // contains copies of the configuration documents. If they change, then we need to refresh
                // the workspace. Otherwise we will not see the changes.
                configService.refresh();
            }
            this.epoch = epoch;
            if (!polled) {
                polled = true;
                notifyAll();
            }
        }
    }
    
    synchronized ConfigEpoch getEpoch() {
        if (!polled) {
            log.debug("Waiting for connection to deployment manager");
            do {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } while (!polled);
        }
        return epoch;
    }
    
    synchronized ConfigServiceWrapper getConfigService() {
        return configService;
    }

    synchronized void incrementRefCount() {
        refCount++;
        if (log.isDebugEnabled()) {
            log.debug("New ref count is " + refCount);
        }
    }

    synchronized void decrementRefCount() {
        refCount--;
        if (log.isDebugEnabled()) {
            log.debug("New ref count is " + refCount);
        }
        if (refCount == 0) {
            log.debug("Destroying DeploymentManagerConnection");
            configService.destroy();
            future.cancel(false);
            factory.removeDeploymentManagerConnection(this);
        }
    }
}
